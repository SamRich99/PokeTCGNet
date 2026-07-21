package it.unipi.poketcgnet.service.impl;

import it.unipi.poketcgnet.dto.DeletedAccountDTO;
import it.unipi.poketcgnet.dto.DeletedTournamentDTO;
import it.unipi.poketcgnet.dto.GymDTO;
import it.unipi.poketcgnet.dto.GymRatingDTO;
import it.unipi.poketcgnet.dto.GymRegistrationRequest;
import it.unipi.poketcgnet.dto.OrganizedTournamentEntryDTO;
import it.unipi.poketcgnet.dto.PasswordChangeRequest;
import it.unipi.poketcgnet.dto.ReviewDTO;
import it.unipi.poketcgnet.dto.ReviewRequest;
import it.unipi.poketcgnet.dto.SliceDTO;
import it.unipi.poketcgnet.model.mongo.Gym;
import it.unipi.poketcgnet.model.mongo.OrganizedTournamentEntry;
import it.unipi.poketcgnet.model.mongo.Participant;
import it.unipi.poketcgnet.model.mongo.Review;
import it.unipi.poketcgnet.model.mongo.Tournament;
import it.unipi.poketcgnet.model.mongo.Trainer;
import it.unipi.poketcgnet.model.neo4j.GymNode;
import it.unipi.poketcgnet.repository.mongo.GymRepository;
import it.unipi.poketcgnet.repository.mongo.TournamentRepository;
import it.unipi.poketcgnet.repository.mongo.TrainerRepository;
import it.unipi.poketcgnet.repository.neo4j.GymGraphRepository;
import it.unipi.poketcgnet.repository.neo4j.TournamentGraphRepository;
import it.unipi.poketcgnet.service.GraphPropagation;
import it.unipi.poketcgnet.service.GymService;
import it.unipi.poketcgnet.service.exception.ResourceAlreadyExistsException;
import it.unipi.poketcgnet.service.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class GymServiceImpl implements GymService {

    private final GymRepository gymRepository;
    private final TrainerRepository trainerRepository;
    private final PasswordEncoder passwordEncoder;
    private final GymGraphRepository gymGraphRepository;
    private final GraphPropagation graphPropagation;
    private final TournamentRepository tournamentRepository;
    private final TournamentGraphRepository tournamentGraphRepository;

    private static final Set<String> PENDING_TOURNAMENT_STATUSES = Set.of(TournamentServiceImpl.STATUS_REGISTRATION,
            TournamentServiceImpl.STATUS_ELABORATION);

    @Value("${app.pagination.gyms:25}")
    private int pageSize;

    public GymServiceImpl(GymRepository gymRepository, TrainerRepository trainerRepository,
            PasswordEncoder passwordEncoder,
            GymGraphRepository gymGraphRepository,
            GraphPropagation graphPropagation,
            TournamentRepository tournamentRepository,
            TournamentGraphRepository tournamentGraphRepository) {
        this.gymRepository = gymRepository;
        this.trainerRepository = trainerRepository;
        this.passwordEncoder = passwordEncoder;
        this.gymGraphRepository = gymGraphRepository;
        this.graphPropagation = graphPropagation;
        this.tournamentRepository = tournamentRepository;
        this.tournamentGraphRepository = tournamentGraphRepository;
    }

    @Override
    public SliceDTO<GymDTO> getGymsPage(int pageNumber) {
        if (pageNumber <= 0) {
            throw new IllegalArgumentException("Numero di pagina non valido: " + pageNumber);
        }
        Slice<Gym> slice = gymRepository.findAllBy(
                PageRequest.of(pageNumber - 1, pageSize, Sort.by("username")));
        return new SliceDTO<>(slice.getContent().stream().map(this::toDTO).toList(),
                slice.hasNext(), slice.hasPrevious());
    }

    @Override
    public GymDTO getGymByUsername(String username) {
        return toDTO(gymRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Gym non trovato: " + username)));
    }

    @Override
    public List<GymRatingDTO> getGymsByPrefectureRankedByRating(String prefecture) {
        return gymRepository.aggregateGymsByPrefectureRankedByRating(prefecture);
    }

    @Override
    public GymDTO registerGym(GymRegistrationRequest request) {
        checkNotAlreadyRegistered(request.getUsername(), request.getEmail());

        Gym gym = Gym.builder()
                .username(request.getUsername())
                .role("gym")
                .email(request.getEmail())
                .password(hashPassword(request.getPassword()))
                .name(request.getShopName())
                .surname(null)
                .birthDate(null)
                .createdAt(LocalDate.now())
                .piva(request.getPiva())
                .shopName(request.getShopName())
                .shopAddress(request.getShopAddress())
                .prefecture(request.getPrefecture())
                .reviews(new ArrayList<>())
                .organizedTournaments(new ArrayList<>())
                .build();

        Gym saved = gymRepository.save(gym);
        graphPropagation.propagate("registerGym " + saved.getUsername(),
                () -> gymGraphRepository
                        .save(new GymNode(saved.getUsername(), saved.getShopName(), saved.getPrefecture())));
        return toDTO(saved);
    }

    @Override
    public List<GymDTO> searchGyms(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Testo di ricerca vuoto");
        }
        // Pattern.quote: l'input dell'utente è un letterale, non un pattern regex
        return gymRepository.searchGyms(Pattern.quote(query.trim()),
                PageRequest.of(0, 25, Sort.by("username")))
                .stream().map(this::toDTO).toList();
    }

    @Override
    public void changePassword(String username, PasswordChangeRequest request) {
        Gym gym = gymRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Gym non trovato: " + username));
        if (!passwordEncoder.matches(request.getOldPassword(), gym.getPassword())) {
            throw new IllegalArgumentException("La password attuale non è corretta");
        }
        gym.setPassword(hashPassword(request.getNewPassword()));
        gymRepository.save(gym);
    }

    @Override
    public GymDTO reviewGym(String gymUsername, String trainerUsername, ReviewRequest request) {
        Gym gym = gymRepository.findByUsername(gymUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Gym non trovato: " + gymUsername));
        Trainer reviewer = trainerRepository.findByUsername(trainerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer non trovato: " + trainerUsername));

        // una sola recensione per trainer: se esiste già viene sostituita
        List<Review> reviews = gym.getReviews() != null ? gym.getReviews() : new ArrayList<>();
        reviews.removeIf(r -> reviewer.getId().equals(r.getUserId()));
        reviews.add(new Review(reviewer.getId(), trainerUsername, request.getTextReview(), request.getScore(),
                LocalDate.now()));
        gym.setReviews(reviews);

        return toDTO(gymRepository.save(gym));
    }

    @Override
    public GymDTO deleteReview(String gymUsername, String reviewerUsername, String requesterUsername, boolean isAdmin) {
        // il trainer può cancellare solo la PROPRIA recensione; l'admin qualsiasi
        if (!isAdmin && !requesterUsername.equals(reviewerUsername)) {
            throw new AccessDeniedException("Un trainer può eliminare solo la propria recensione");
        }
        Gym gym = gymRepository.findByUsername(gymUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Gym non trovato: " + gymUsername));

        String reviewerId = trainerRepository.findByUsername(reviewerUsername).map(Trainer::getId).orElse(null);
        List<Review> reviews = gym.getReviews() != null ? gym.getReviews() : new ArrayList<>();
        boolean removed = reviews.removeIf(r -> reviewerId != null
                ? reviewerId.equals(r.getUserId())
                : reviewerUsername.equals(r.getUsername()));
        if (!removed) {
            throw new ResourceNotFoundException("Recensione non trovata per: " + reviewerUsername);
        }
        gym.setReviews(reviews);
        return toDTO(gymRepository.save(gym));
    }

    @Override
    public DeletedAccountDTO deleteAccount(String targetUsername) {
        Gym gym = gymRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Gym non trovato: " + targetUsername));

        List<DeletedTournamentDTO> cancelledTournaments = cancelPendingTournamentsOf(gym.getId());
        graphPropagation.propagate("deleteAccountGym " + targetUsername,
                () -> gymGraphRepository.deleteById(targetUsername));

        gymRepository.delete(gym);
        return new DeletedAccountDTO(targetUsername, cancelledTournaments);
    }

    // I tornei registration/elaboration non hanno ancora punti assegnati, quindi
    // vanno annullati per intero
    private List<DeletedTournamentDTO> cancelPendingTournamentsOf(String gymId) {
        List<Tournament> pending = tournamentRepository.findByGymIdOrderByStartDateAsc(gymId).stream()
                .filter(t -> PENDING_TOURNAMENT_STATUSES.contains(t.getStatus()))
                .toList();

        List<DeletedTournamentDTO> cancelled = new ArrayList<>();
        for (Tournament t : pending) {
            List<String> emails = t.getParticipants().stream().map(Participant::getEmail).toList();
            tournamentRepository.deleteById(t.getId());
            graphPropagation.propagate("cascadeDeleteTournament " + t.getId(),
                    () -> tournamentGraphRepository.deleteById(t.getId()));
            cancelled.add(new DeletedTournamentDTO(t.getId(), t.getName(), emails));
        }
        return cancelled;
    }

    private void checkNotAlreadyRegistered(String username, String email) {
        if (gymRepository.existsByUsername(username) || trainerRepository.existsByUsername(username)) {
            throw new ResourceAlreadyExistsException("Username già registrato: " + username);
        }
        if (gymRepository.findByEmail(email).isPresent() || trainerRepository.findByEmail(email).isPresent()) {
            throw new ResourceAlreadyExistsException("Email già registrata: " + email);
        }
    }

    private String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    private GymDTO toDTO(Gym gym) {
        List<ReviewDTO> reviews = gym.getReviews().stream().map(this::toDTO).toList();
        List<OrganizedTournamentEntryDTO> organizedTournaments = gym.getOrganizedTournaments().stream()
                .map(this::toDTO)
                .toList();

        return GymDTO.builder()
                .username(gym.getUsername())
                .role(gym.getRole())
                .email(gym.getEmail())
                .name(gym.getName())
                .surname(gym.getSurname())
                .birthDate(gym.getBirthDate())
                .createdAt(gym.getCreatedAt())
                .piva(gym.getPiva())
                .shopName(gym.getShopName())
                .shopAddress(gym.getShopAddress())
                .prefecture(gym.getPrefecture())
                .reviews(reviews)
                .organizedTournaments(organizedTournaments)
                .build();
    }

    private ReviewDTO toDTO(Review review) {
        return new ReviewDTO(review.getUsername(), review.getTextReview(), review.getScore(), review.getDateReview());
    }

    private OrganizedTournamentEntryDTO toDTO(OrganizedTournamentEntry entry) {
        return new OrganizedTournamentEntryDTO(entry.getTournamentId(), entry.getName(), entry.getDate(),
                entry.getPlayers(), entry.getStatus());
    }
}
