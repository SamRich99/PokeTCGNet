package it.unipi.poketcgnet.service.impl;

import it.unipi.poketcgnet.dto.DeletedAccountDTO;
import it.unipi.poketcgnet.dto.MonthAnalyticsDTO;
import it.unipi.poketcgnet.dto.MonthlySubscribesDTO;
import it.unipi.poketcgnet.dto.PasswordChangeRequest;
import it.unipi.poketcgnet.dto.SliceDTO;
import it.unipi.poketcgnet.dto.TournamentHistoryEntryDTO;
import it.unipi.poketcgnet.dto.TrainerDTO;
import it.unipi.poketcgnet.dto.TrainerKpiDTO;
import it.unipi.poketcgnet.dto.TrainerRegistrationRequest;
import it.unipi.poketcgnet.model.mongo.Deck;
import it.unipi.poketcgnet.model.mongo.Tournament;
import it.unipi.poketcgnet.model.mongo.TournamentHistoryEntry;
import it.unipi.poketcgnet.model.mongo.Trainer;
import it.unipi.poketcgnet.model.neo4j.TrainerNode;
import it.unipi.poketcgnet.repository.mongo.DeckRepository;
import it.unipi.poketcgnet.repository.mongo.GymRepository;
import it.unipi.poketcgnet.repository.mongo.TournamentRepository;
import it.unipi.poketcgnet.repository.mongo.TrainerRepository;
import it.unipi.poketcgnet.repository.neo4j.TournamentGraphRepository;
import it.unipi.poketcgnet.repository.neo4j.TrainerGraphRepository;
import it.unipi.poketcgnet.service.GraphPropagation;
import it.unipi.poketcgnet.service.MonthAnalyticsService;
import it.unipi.poketcgnet.service.TrainerService;
import it.unipi.poketcgnet.service.exception.ResourceAlreadyExistsException;
import it.unipi.poketcgnet.service.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class TrainerServiceImpl implements TrainerService {

    private final TrainerRepository trainerRepository;
    private final GymRepository gymRepository;
    private final PasswordEncoder passwordEncoder;
    private final TrainerGraphRepository trainerGraphRepository;
    private final GraphPropagation graphPropagation;
    private final TournamentRepository tournamentRepository;
    private final DeckRepository deckRepository;
    private final TournamentGraphRepository tournamentGraphRepository;
    private final MonthAnalyticsService monthAnalyticsService;
    private final MongoTemplate mongoTemplate;

    // Soglia "pro" (recomputeProStatus): top 5% dei trainer attivi nella stagione
    // corrente — stessa definizione usata dal seed offline (transform.py, pro_cut).
    private static final double PRO_PERCENTILE = 0.05;

    @Value("${app.pagination.trainers:25}")
    private int pageSize;

    public TrainerServiceImpl(TrainerRepository trainerRepository, GymRepository gymRepository,
            PasswordEncoder passwordEncoder,
            TrainerGraphRepository trainerGraphRepository,
            GraphPropagation graphPropagation,
            TournamentRepository tournamentRepository,
            DeckRepository deckRepository,
            TournamentGraphRepository tournamentGraphRepository,
            MonthAnalyticsService monthAnalyticsService,
            MongoTemplate mongoTemplate) {
        this.trainerRepository = trainerRepository;
        this.gymRepository = gymRepository;
        this.passwordEncoder = passwordEncoder;
        this.trainerGraphRepository = trainerGraphRepository;
        this.graphPropagation = graphPropagation;
        this.tournamentRepository = tournamentRepository;
        this.deckRepository = deckRepository;
        this.tournamentGraphRepository = tournamentGraphRepository;
        this.monthAnalyticsService = monthAnalyticsService;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public SliceDTO<TrainerDTO> getTrainersPage(int pageNumber) {
        if (pageNumber <= 0) {
            throw new IllegalArgumentException("Numero di pagina non valido: " + pageNumber);
        }
        Slice<Trainer> slice = trainerRepository.findAllBy(
                PageRequest.of(pageNumber - 1, pageSize, Sort.by("username")));
        return new SliceDTO<>(slice.getContent().stream().map(this::toDTO).toList(),
                slice.hasNext(), slice.hasPrevious());
    }

    @Override
    public TrainerDTO getTrainerByUsername(String username) {
        return toDTO(trainerRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer non trovato: " + username)));
    }

    @Override
    public TrainerDTO registerTrainer(TrainerRegistrationRequest request) {
        checkNotAlreadyRegistered(request.getUsername(), request.getEmail());

        Trainer trainer = Trainer.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(hashPassword(request.getPassword()))
                .name(request.getName())
                .surname(request.getSurname())
                .birthDate(request.getBirthDate())
                .createdAt(LocalDate.now())
                .prefecture(request.getPrefecture())
                .leaguePoints(0)
                .currentSeasonPoints(0)
                .bestSeasonPoints(0)
                .isPro(false)
                .tournamentHistory(new ArrayList<>())
                .build();

        Trainer saved = trainerRepository.save(trainer);
        // Dual-write Mongo-first (scelta AP del progetto): il nodo grafo replica solo
        // le property lette dalle query on-graph; name = "nome cognome" concatenati
        graphPropagation.propagate("registerTrainer " + saved.getUsername(),
                () -> trainerGraphRepository.save(new TrainerNode(saved.getUsername(),
                        saved.getName() + " " + saved.getSurname(), saved.getPrefecture(), saved.getIsPro())));
        return toDTO(saved);
    }

    @Override
    public List<MonthlySubscribesDTO> getMonthlySubscribes() {
        List<MonthAnalyticsDTO> recent = monthAnalyticsService.getRecentMonths();
        List<MonthlySubscribesDTO> result = new ArrayList<>();
        for (int i = 0; i < recent.size() && i < 12; i++) {
            Integer current = recent.get(i).getNewSubscribes();
            Integer delta = (i + 1 < recent.size()) ? current - recent.get(i + 1).getNewSubscribes() : null;
            result.add(new MonthlySubscribesDTO(recent.get(i).getId(), current, delta));
        }
        return result;
    }

    @Override
    public List<TrainerKpiDTO> getTrainerKpi(String username) {
        if (trainerRepository.findByUsername(username).isEmpty()) {
            throw new ResourceNotFoundException("Trainer non trovato: " + username);
        }
        return trainerRepository.aggregateTrainerKpi(username);
    }

    @Override
    public List<TrainerDTO> searchTrainers(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Testo di ricerca vuoto");
        }
        // Pattern.quote: l'input dell'utente è un letterale, non un pattern regex
        return trainerRepository.searchTrainers(Pattern.quote(query.trim()),
                PageRequest.of(0, 25, Sort.by("username")))
                .stream().map(this::toDTO).toList();
    }

    @Override
    public void changePassword(String username, PasswordChangeRequest request) {
        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer non trovato: " + username));
        if (!passwordEncoder.matches(request.getOldPassword(), trainer.getPassword())) {
            throw new IllegalArgumentException("La password attuale non è corretta");
        }
        trainer.setPassword(hashPassword(request.getNewPassword()));
        trainerRepository.save(trainer);
    }

    @Override
    public DeletedAccountDTO deleteAccount(String targetUsername) {
        Trainer trainer = trainerRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer non trovato: " + targetUsername));

        removeTrainerFromOpenTournaments(trainer.getId(), targetUsername);
        softDeleteDecksOf(trainer.getId());
        graphPropagation.propagate("deleteAccountTrainer " + targetUsername,
                () -> trainerGraphRepository.deleteById(targetUsername));

        trainerRepository.delete(trainer);
        return new DeletedAccountDTO(targetUsername, List.of());
    }

    // Fine stagione (admin): azzera currentSeasonPoints e isPro per tutti i trainer
    // in
    // un solo round-trip Mongo (updateMulti, niente findAll+loop su ~12k
    // documenti).
    // leaguePoints/bestSeasonPoints non si toccano (carriera/record storico).
    @Override
    public void endSeason() {
        mongoTemplate.updateMulti(new Query(),
                new Update().set("currentSeasonPoints", 0).set("isPro", false),
                Trainer.class);
        graphPropagation.propagate("endSeason", trainerGraphRepository::resetAllIsPro);
    }

    // Ricalcola isPro sul top 5% dei trainer attivi (currentSeasonPoints > 0),
    // stessa
    // definizione del seed offline (transform.py). Richiamabile in qualunque
    // momento
    // della stagione, indipendente da endSeason(). I pareggi esatti sulla soglia
    // diventano tutti pro (stesso comportamento del dataset originale).
    @Override
    public void recomputeProStatus() {
        long active = trainerRepository.countByCurrentSeasonPointsGreaterThan(0);
        if (active == 0) {
            mongoTemplate.updateMulti(new Query(), new Update().set("isPro", false), Trainer.class);
            graphPropagation.propagate("recomputeProStatus", trainerGraphRepository::resetAllIsPro);
            return;
        }

        long cutoffIndex = (long) Math.ceil(active * PRO_PERCENTILE) - 1;
        Query rankQuery = Query.query(Criteria.where("currentSeasonPoints").gt(0))
                .with(Sort.by(Sort.Direction.DESC, "currentSeasonPoints"))
                .skip(cutoffIndex).limit(1);
        Trainer edge = mongoTemplate.findOne(rankQuery, Trainer.class);
        int threshold = edge.getCurrentSeasonPoints();

        mongoTemplate.updateMulti(Query.query(Criteria.where("currentSeasonPoints").gte(threshold)),
                new Update().set("isPro", true), Trainer.class);
        mongoTemplate.updateMulti(Query.query(Criteria.where("currentSeasonPoints").lt(threshold)),
                new Update().set("isPro", false), Trainer.class);

        List<String> proUsernames = trainerRepository.findByCurrentSeasonPointsGreaterThanEqual(threshold)
                .stream().map(Trainer::getUsername).toList();
        graphPropagation.propagate("recomputeProStatus", () -> {
            trainerGraphRepository.resetAllIsPro();
            trainerGraphRepository.promoteToPro(proUsernames);
        });
    }

    private void removeTrainerFromOpenTournaments(String trainerId, String trainerUsername) {
        List<Tournament> joined = tournamentRepository.findByParticipants_UserIdOrderByStartDateAsc(trainerId).stream()
                .filter(t -> TournamentServiceImpl.STATUS_REGISTRATION.equals(t.getStatus()))
                .toList();

        for (Tournament t : joined) {
            t.getParticipants().removeIf(p -> trainerId.equals(p.getUserId()));
            tournamentRepository.save(t);
            graphPropagation.propagate("cascadeLeaveTournament " + trainerUsername + " -> " + t.getId(),
                    () -> trainerGraphRepository.leaveTournament(trainerUsername, t.getId()));
        }
    }

    private void softDeleteDecksOf(String trainerId) {
        for (Deck deck : deckRepository.findByOwnerId(trainerId)) {
            deck.setDeleted(true);
            deckRepository.save(deck);
        }
    }

    private void checkNotAlreadyRegistered(String username, String email) {
        if (trainerRepository.existsByUsername(username) || gymRepository.existsByUsername(username)) {
            throw new ResourceAlreadyExistsException("Username già registrato: " + username);
        }
        if (trainerRepository.findByEmail(email).isPresent() || gymRepository.findByEmail(email).isPresent()) {
            throw new ResourceAlreadyExistsException("Email già registrata: " + email);
        }
    }

    private String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    private TrainerDTO toDTO(Trainer trainer) {
        List<TournamentHistoryEntryDTO> tournamentHistory = trainer.getTournamentHistory().stream()
                .map(this::toDTO)
                .toList();

        return TrainerDTO.builder()
                .username(trainer.getUsername())
                .role("trainer")
                .email(trainer.getEmail())
                .name(trainer.getName())
                .surname(trainer.getSurname())
                .birthDate(trainer.getBirthDate())
                .createdAt(trainer.getCreatedAt())
                .prefecture(trainer.getPrefecture())
                .leaguePoints(trainer.getLeaguePoints())
                .currentSeasonPoints(trainer.getCurrentSeasonPoints())
                .bestSeasonPoints(trainer.getBestSeasonPoints())
                .isPro(trainer.getIsPro())
                .tournamentHistory(tournamentHistory)
                .build();
    }

    private TournamentHistoryEntryDTO toDTO(TournamentHistoryEntry entry) {
        return new TournamentHistoryEntryDTO(
                entry.getTournamentId(),
                entry.getTournamentName(),
                entry.getDate(),
                entry.getArchetypeName(),
                entry.getDeckId(),
                entry.getFinalStanding(),
                entry.getLeaguePointEarned());
    }
}
