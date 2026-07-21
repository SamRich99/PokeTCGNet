package it.unipi.poketcgnet.service.impl;

import it.unipi.poketcgnet.dto.DeletedTournamentDTO;
import it.unipi.poketcgnet.dto.FollowedGymTournamentDTO;
import it.unipi.poketcgnet.dto.MonthAnalyticsDTO;
import it.unipi.poketcgnet.dto.MonthlyTournamentsDTO;
import it.unipi.poketcgnet.dto.ParticipantDTO;
import it.unipi.poketcgnet.dto.ParticipationRequest;
import it.unipi.poketcgnet.dto.SliceDTO;
import it.unipi.poketcgnet.dto.StandingsRequest;
import it.unipi.poketcgnet.dto.TournamentDTO;
import it.unipi.poketcgnet.dto.TournamentRequest;
import it.unipi.poketcgnet.dto.TournamentSearchFilter;
import it.unipi.poketcgnet.dto.UpdatedTournamentDTO;
import it.unipi.poketcgnet.model.mongo.Deck;
import it.unipi.poketcgnet.model.mongo.Gym;
import it.unipi.poketcgnet.model.mongo.OrganizedTournamentEntry;
import it.unipi.poketcgnet.model.mongo.Participant;
import it.unipi.poketcgnet.model.mongo.Tournament;
import it.unipi.poketcgnet.model.mongo.TournamentHistoryEntry;
import it.unipi.poketcgnet.model.mongo.Trainer;
import it.unipi.poketcgnet.repository.mongo.DeckRepository;
import it.unipi.poketcgnet.repository.mongo.GymRepository;
import it.unipi.poketcgnet.repository.mongo.TournamentRepository;
import it.unipi.poketcgnet.repository.mongo.TrainerRepository;
import it.unipi.poketcgnet.repository.neo4j.GymGraphRepository;
import it.unipi.poketcgnet.repository.neo4j.TournamentGraphRepository;
import it.unipi.poketcgnet.repository.neo4j.TrainerGraphRepository;
import it.unipi.poketcgnet.service.GraphPropagation;
import it.unipi.poketcgnet.service.MonthAnalyticsService;
import it.unipi.poketcgnet.service.TournamentService;
import it.unipi.poketcgnet.service.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class TournamentServiceImpl implements TournamentService {

    public static final String STATUS_REGISTRATION = "registration";
    public static final String STATUS_ELABORATION = "elaboration";
    public static final String STATUS_CONCLUDED = "concluded";

    private final TournamentRepository tournamentRepository;
    private final TrainerRepository trainerRepository;
    private final GymRepository gymRepository;
    private final DeckRepository deckRepository;
    private final TrainerGraphRepository trainerGraphRepository;
    private final GymGraphRepository gymGraphRepository;
    private final TournamentGraphRepository tournamentGraphRepository;
    private final GraphPropagation graphPropagation;
    private final MongoTemplate mongoTemplate;
    private final MonthAnalyticsService monthAnalyticsService;

    @Value("${app.pagination.tournaments:20}")
    private int pageSize;

    public TournamentServiceImpl(TournamentRepository tournamentRepository,
            TrainerRepository trainerRepository,
            GymRepository gymRepository,
            DeckRepository deckRepository,
            TrainerGraphRepository trainerGraphRepository,
            GymGraphRepository gymGraphRepository,
            TournamentGraphRepository tournamentGraphRepository,
            GraphPropagation graphPropagation,
            MongoTemplate mongoTemplate,
            MonthAnalyticsService monthAnalyticsService) {
        this.tournamentRepository = tournamentRepository;
        this.trainerRepository = trainerRepository;
        this.gymRepository = gymRepository;
        this.deckRepository = deckRepository;
        this.trainerGraphRepository = trainerGraphRepository;
        this.gymGraphRepository = gymGraphRepository;
        this.tournamentGraphRepository = tournamentGraphRepository;
        this.graphPropagation = graphPropagation;
        this.mongoTemplate = mongoTemplate;
        this.monthAnalyticsService = monthAnalyticsService;
    }

    @Override
    public SliceDTO<TournamentDTO> getTournamentsPage(int pageNumber) {
        if (pageNumber <= 0) {
            throw new IllegalArgumentException("Numero di pagina non valido: " + pageNumber);
        }
        Slice<Tournament> slice = tournamentRepository.findAllBy(
                PageRequest.of(pageNumber - 1, pageSize, Sort.by("_id")));
        return new SliceDTO<>(slice.getContent().stream().map(this::toDTO).toList(),
                slice.hasNext(), slice.hasPrevious());
    }

    @Override
    public TournamentDTO getTournamentById(String id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo non trovato: " + id));
        return toDTO(tournament);
    }

    @Override
    public List<TournamentDTO> searchTournamentsByName(String name) {
        return tournamentRepository.findByNameContainingIgnoreCaseOrderByStartDateAsc(name).stream().map(this::toDTO)
                .toList();
    }

    // lista tornei registrati dai followed gym (homepage trainer)
    @Override
    public List<FollowedGymTournamentDTO> getRegistrationTournamentsFromFollowedGyms(String trainerUsername) {
        return trainerGraphRepository.findRegistrationTournamentsFromFollowedGyms(trainerUsername);
    }

    @Override
    public List<TournamentDTO> getTrendTournaments(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limite non valido: " + limit);
        }
        return tournamentRepository.findByStatusOrderByLeaguePointRequestDesc(
                STATUS_CONCLUDED, PageRequest.of(0, limit))
                .stream().map(this::toDTO).toList();
    }

    // analytics admin: tornei conclusi per mese con delta
    @Override
    public List<MonthlyTournamentsDTO> getMonthlyTournaments() {
        List<MonthAnalyticsDTO> recent = monthAnalyticsService.getRecentMonths();
        List<MonthlyTournamentsDTO> result = new ArrayList<>();
        for (int i = 0; i < recent.size() && i < 12; i++) {
            Integer current = recent.get(i).getNewTournaments();
            Integer delta = (i + 1 < recent.size()) ? current - recent.get(i + 1).getNewTournaments() : null;
            result.add(new MonthlyTournamentsDTO(recent.get(i).getId(), current, delta));
        }
        return result;
    }

    // ricerca avanzata tornei (filtri dinamici)
    @Override
    public SliceDTO<TournamentDTO> searchTournamentsAdvanced(TournamentSearchFilter filter, int pageNumber) {
        if (pageNumber <= 0) {
            throw new IllegalArgumentException("Numero di pagina non valido: " + pageNumber);
        }
        List<Criteria> criteria = new ArrayList<>();

        if (filter.getName() != null && !filter.getName().isBlank()) {
            criteria.add(Criteria.where("name").regex(Pattern.quote(filter.getName()), "i"));
        }
        if (filter.getStatus() != null) {
            criteria.add(Criteria.where("status").is(filter.getStatus()));
        }
        if (filter.getGymUsername() != null) {
            String gymId = gymRepository.findByUsername(filter.getGymUsername())
                    .map(Gym::getId).orElse("");
            criteria.add(Criteria.where("gymId").is(gymId));
        }
        if (filter.getPrefecture() != null) {
            List<String> gymIds = gymRepository.findByPrefecture(filter.getPrefecture()).stream()
                    .map(Gym::getId)
                    .toList();
            criteria.add(Criteria.where("gymId").in(gymIds));
        }
        addRangeCriteria(criteria, "startDate",
                filter.getStartDateFrom() != null ? filter.getStartDateFrom().toString() : null,
                filter.getStartDateTo() != null ? filter.getStartDateTo().toString() : null);
        addRangeCriteria(criteria, "leaguePointRequest", filter.getMinLeaguePointRequest(),
                filter.getMaxLeaguePointRequest());
        addRangeCriteria(criteria, "limitParticipants", filter.getMinLimitParticipants(),
                filter.getMaxLimitParticipants());

        Query query = new Query();
        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }
        query.with(Sort.by("_id")).skip((long) (pageNumber - 1) * pageSize).limit(pageSize + 1);
        List<Tournament> results = mongoTemplate.find(query, Tournament.class);
        boolean hasNext = results.size() > pageSize;
        List<TournamentDTO> content = results.stream()
                .limit(pageSize)
                .map(this::toDTO)
                .toList();
        return new SliceDTO<>(content, hasNext, pageNumber > 1);
    }

    private <T extends Comparable<T>> void addRangeCriteria(List<Criteria> criteria, String field, T from, T to) {
        if (from == null && to == null) {
            return;
        }
        Criteria c = Criteria.where(field);
        if (from != null) {
            c = c.gte(from);
        }
        if (to != null) {
            c = c.lte(to);
        }
        criteria.add(c);
    }

    @Override
    public TournamentDTO createTournament(String gymUsername, TournamentRequest request) {
        Gym gym = loadGym(gymUsername);
        validateDates(request);

        Tournament t = new Tournament();
        t.setName(request.getName());
        t.setGymId(gym.getId());
        t.setGymUsername(gym.getUsername());
        t.setGymShopName(gym.getShopName());
        t.setStartDate(request.getStartDate());
        t.setStatus(STATUS_REGISTRATION);
        t.setLeaguePointRequest(request.getLeaguePointRequest());
        t.setLimitParticipants(request.getLimitParticipants());
        t.setLeaguePointRewardFirst(rewardFirst(request.getLimitParticipants(), request.getLeaguePointRequest()));
        t.setParticipants(new ArrayList<>());

        // Dual-write Mongo-first (scelta AP): prima il documento (che genera l'_id),
        // poi nodo Tournament + arco ORGANIZES sul grafo
        Tournament saved = tournamentRepository.save(t);
        graphPropagation.propagate("createTournament " + saved.getId(),
                () -> gymGraphRepository.createTournament(saved.getGymUsername(), saved.getId(),
                        saved.getName(), saved.getStartDate(), saved.getStatus()));

        List<OrganizedTournamentEntry> organized = gym.getOrganizedTournaments() != null ? gym.getOrganizedTournaments()
                : new ArrayList<>();
        organized.add(new OrganizedTournamentEntry(saved.getId(), saved.getName(), saved.getStartDate(),
                0, saved.getStatus()));
        gym.setOrganizedTournaments(organized);
        gymRepository.save(gym);

        return toDTO(saved);
    }

    @Override
    public UpdatedTournamentDTO updateTournament(String id, String gymUsername, TournamentRequest request) {
        Tournament t = loadOwnedTournament(id, gymUsername);
        requireStatus(t, STATUS_REGISTRATION, "modificare");
        validateDates(request);

        t.setName(request.getName());
        t.setStartDate(request.getStartDate());
        t.setLimitParticipants(request.getLimitParticipants());
        t.setLeaguePointRequest(request.getLeaguePointRequest());
        t.setLeaguePointRewardFirst(rewardFirst(request.getLimitParticipants(), request.getLeaguePointRequest()));

        Tournament saved = tournamentRepository.save(t);

        Gym gym = loadGym(gymUsername);
        updateOrganizedTournamentEntry(gym, saved, saved.getStatus(), saved.getParticipants().size());
        gymRepository.save(gym);

        List<String> emails = saved.getParticipants().stream().map(Participant::getEmail).toList();
        return new UpdatedTournamentDTO(toDTO(saved), emails);
    }

    @Override
    public TournamentDTO closeRegistration(String id, String gymUsername) {
        Tournament t = loadOwnedTournament(id, gymUsername);
        requireStatus(t, STATUS_REGISTRATION, "chiudere le iscrizioni di");

        t.setStatus(STATUS_ELABORATION);
        Tournament saved = tournamentRepository.save(t);
        graphPropagation.propagate("closeRegistration " + saved.getId(),
                () -> tournamentGraphRepository.updateStatus(saved.getId(), saved.getStatus()));

        Gym gym = loadGym(gymUsername);
        updateOrganizedTournamentEntry(gym, saved, saved.getStatus(), saved.getParticipants().size());
        gymRepository.save(gym);

        return toDTO(saved);
    }

    @Override
    public TournamentDTO publishStandings(String id, String gymUsername, StandingsRequest request) {
        Tournament t = loadOwnedTournament(id, gymUsername);
        requireStatus(t, STATUS_ELABORATION, "pubblicare la classifica di");

        List<Participant> participants = t.getParticipants();
        int n = participants.size();
        if (n == 0) {
            throw new IllegalStateException("Il torneo non ha partecipanti: impossibile pubblicare una classifica");
        }

        // la classifica deve coprire esattamente i partecipanti, piazzamenti 1..n
        Map<String, Integer> standingByUsername = new HashMap<>();
        boolean[] taken = new boolean[n + 1];
        for (StandingsRequest.StandingEntry e : request.getStandings()) {
            if (standingByUsername.put(e.getUsername(), e.getFinalStanding()) != null) {
                throw new IllegalArgumentException("Username duplicato in classifica: " + e.getUsername());
            }
            int s = e.getFinalStanding();
            if (s < 1 || s > n) {
                throw new IllegalArgumentException("Piazzamento fuori range 1.." + n + ": " + s);
            }
            if (taken[s]) {
                throw new IllegalArgumentException("Piazzamento duplicato: " + s);
            }
            taken[s] = true;
        }
        if (standingByUsername.size() != n) {
            throw new IllegalArgumentException("La classifica deve coprire tutti i " + n + " partecipanti");
        }
        for (Participant p : participants) {
            if (!standingByUsername.containsKey(p.getUsername())) {
                throw new IllegalArgumentException("Partecipante mancante in classifica: " + p.getUsername());
            }
        }

        // reward ricalcolato sul numero reale di partecipanti
        int reward = rewardFirst(n, t.getLeaguePointRequest());
        int rounds = Math.max(1, ceilLog2(Math.max(n, 2)));
        t.setLeaguePointRewardFirst(reward);

        for (Participant p : participants) {
            int placing = standingByUsername.get(p.getUsername());
            int wins = Math.max(0, rounds - ceilLog2(placing));
            p.setFinalStanding(placing);
            p.setLeaguePointEarned(earned(placing, reward, wins));
        }
        t.setStatus(STATUS_CONCLUDED);

        Tournament saved = tournamentRepository.save(t);

        for (Participant p : saved.getParticipants()) {
            if (p.getUserId() == null) {
                continue; // participant orfano
            }
            trainerRepository.findById(p.getUserId())
                    .ifPresent(trainer -> applyResultToTrainer(trainer, saved, p));
        }

        Gym gym = loadGym(gymUsername);
        updateOrganizedTournamentEntry(gym, saved, saved.getStatus(), n);
        gymRepository.save(gym);

        graphPropagation.propagate("publishStandings " + saved.getId(),
                () -> tournamentGraphRepository.updateStatus(saved.getId(), saved.getStatus()));
        return toDTO(saved);
    }

    @Override
    public DeletedTournamentDTO deleteTournament(String id, String requesterUsername, boolean isAdmin) {
        Tournament t = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo non trovato: " + id));
        if (!isAdmin && !requesterUsername.equals(t.getGymUsername())) {
            throw new AccessDeniedException("Il torneo appartiene a un altro gym");
        }

        if (STATUS_CONCLUDED.equals(t.getStatus())) {
            if (!isAdmin) {
                throw new AccessDeniedException("Un torneo concluso può essere eliminato solo dall'admin");
            }
            tournamentRepository.deleteById(id);
            return new DeletedTournamentDTO(t.getId(), t.getName(), List.of());
        }
        List<String> emails = isAdmin ? List.of()
                : t.getParticipants().stream().map(Participant::getEmail).toList();
        tournamentRepository.deleteById(id);
        graphPropagation.propagate("deleteTournament " + id,
                () -> tournamentGraphRepository.deleteById(id));

        gymRepository.findById(t.getGymId()).ifPresent(gym -> {
            if (gym.getOrganizedTournaments() != null
                    && gym.getOrganizedTournaments().removeIf(e -> id.equals(e.getTournamentId()))) {
                gymRepository.save(gym);
            }
        });

        return new DeletedTournamentDTO(t.getId(), t.getName(), emails);
    }

    @Override
    public TournamentDTO joinTournament(String id, String trainerUsername, ParticipationRequest request) {
        Tournament t = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo non trovato: " + id));
        requireStatus(t, STATUS_REGISTRATION, "iscriversi a");

        Trainer trainer = trainerRepository.findByUsername(trainerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer non trovato: " + trainerUsername));

        // identità (iscrizione esistente, ownership del deck): sempre su id interno
        List<Participant> participants = t.getParticipants();
        if (participants.stream().anyMatch(p -> trainer.getId().equals(p.getUserId()))) {
            throw new IllegalStateException("Sei già iscritto a questo torneo");
        }
        if (participants.size() >= t.getLimitParticipants()) {
            throw new IllegalStateException("Il torneo ha già raggiunto il limite di "
                    + t.getLimitParticipants() + " partecipanti");
        }

        Deck deck = deckRepository.findById(request.getDeckId())
                .orElseThrow(() -> new ResourceNotFoundException("Deck non trovato: " + request.getDeckId()));
        if (!trainer.getId().equals(deck.getOwnerId())) {
            throw new AccessDeniedException("Il deck appartiene a un altro trainer");
        }
        if (Boolean.TRUE.equals(deck.getDeleted())) {
            throw new IllegalStateException("Il deck è stato eliminato o sostituito da una nuova versione");
        }

        participants.add(new Participant(trainer.getId(), trainerUsername, trainer.getEmail(),
                deck.getId(), deck.getArchetypeName(), null, null));

        Tournament saved = tournamentRepository.save(t);
        graphPropagation.propagate("joinTournament " + trainerUsername + " -> " + saved.getId(),
                () -> trainerGraphRepository.participateInTournament(trainerUsername, saved.getId()));
        return toDTO(saved);
    }

    @Override
    public TournamentDTO leaveTournament(String id, String trainerUsername) {
        Tournament t = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo non trovato: " + id));
        requireStatus(t, STATUS_REGISTRATION, "disiscriversi da");

        // il chiamante si identifica: match sull'id interno, non sullo username
        String trainerId = trainerRepository.findByUsername(trainerUsername)
                .map(Trainer::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer non trovato: " + trainerUsername));
        boolean removed = t.getParticipants().removeIf(p -> trainerId.equals(p.getUserId()));
        if (!removed) {
            throw new ResourceNotFoundException("Non sei iscritto a questo torneo");
        }

        Tournament saved = tournamentRepository.save(t);
        graphPropagation.propagate("leaveTournament " + trainerUsername + " -> " + saved.getId(),
                () -> trainerGraphRepository.leaveTournament(trainerUsername, saved.getId()));
        return toDTO(saved);
    }

    @Override
    public TournamentDTO removeParticipant(String id, String gymUsername, String participantUsername) {
        Tournament t = loadOwnedTournament(id, gymUsername);
        if (!STATUS_REGISTRATION.equals(t.getStatus()) && !STATUS_ELABORATION.equals(t.getStatus())) {
            throw new IllegalStateException("Impossibile rimuovere un iscritto da un torneo '"
                    + t.getStatus() + "'");
        }

        boolean removed = t.getParticipants().removeIf(p -> participantUsername.equals(p.getUsername()));
        if (!removed) {
            throw new ResourceNotFoundException("Il trainer non è iscritto a questo torneo: " + participantUsername);
        }

        Tournament saved = tournamentRepository.save(t);
        graphPropagation.propagate("removeParticipant " + participantUsername + " -> " + saved.getId(),
                () -> trainerGraphRepository.leaveTournament(participantUsername, saved.getId()));
        return toDTO(saved);
    }

    private void applyResultToTrainer(Trainer trainer, Tournament tournament, Participant p) {
        List<TournamentHistoryEntry> history = trainer.getTournamentHistory() != null ? trainer.getTournamentHistory()
                : new ArrayList<>();
        boolean alreadyApplied = history.stream()
                .anyMatch(h -> tournament.getId().equals(h.getTournamentId()));
        if (alreadyApplied) {
            return;
        }
        history.add(new TournamentHistoryEntry(
                tournament.getId(),
                tournament.getName(),
                tournament.getStartDate(),
                p.getArchetypeName(),
                p.getDeckId(),
                p.getFinalStanding(),
                p.getLeaguePointEarned()));
        trainer.setTournamentHistory(history);
        trainer.setLeaguePoints(nz(trainer.getLeaguePoints()) + p.getLeaguePointEarned());
        trainer.setCurrentSeasonPoints(nz(trainer.getCurrentSeasonPoints()) + p.getLeaguePointEarned());
        if (trainer.getCurrentSeasonPoints() > nz(trainer.getBestSeasonPoints())) {
            trainer.setBestSeasonPoints(trainer.getCurrentSeasonPoints());
        }
        trainerRepository.save(trainer);
    }

    private void updateOrganizedTournamentEntry(Gym gym, Tournament tournament, String status, int players) {
        List<OrganizedTournamentEntry> organized = gym.getOrganizedTournaments() != null ? gym.getOrganizedTournaments()
                : new ArrayList<>();
        OrganizedTournamentEntry entry = organized.stream()
                .filter(e -> tournament.getId().equals(e.getTournamentId()))
                .findFirst()
                .orElseGet(() -> {
                    OrganizedTournamentEntry created = new OrganizedTournamentEntry(
                            tournament.getId(), tournament.getName(), tournament.getStartDate(), players, status);
                    organized.add(created);
                    return created;
                });
        entry.setName(tournament.getName());
        entry.setDate(tournament.getStartDate());
        entry.setStatus(status);
        entry.setPlayers(players);
        gym.setOrganizedTournaments(organized);
    }

    private int rewardFirst(int n, int request) {
        return (int) Math.rint(2.0 * n * (1.0 + request / 200.0));
    }

    private int earned(int placing, int rewardFirst, int wins) {
        if (placing == 1)
            return rewardFirst;
        if (placing == 2)
            return (int) (rewardFirst * 0.6);
        if (placing <= 4)
            return (int) (rewardFirst * 0.4);
        if (placing <= 8)
            return (int) (rewardFirst * 0.25);
        if (placing <= 16)
            return (int) (rewardFirst * 0.1);
        return 2 * wins;
    }

    private int ceilLog2(int x) {
        return x <= 1 ? 0 : 32 - Integer.numberOfLeadingZeros(x - 1);
    }

    private int nz(Integer value) {
        return value != null ? value : 0;
    }

    private Gym loadGym(String gymUsername) {
        return gymRepository.findByUsername(gymUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Gym non trovato: " + gymUsername));
    }

    private Tournament loadOwnedTournament(String id, String gymUsername) {
        Tournament t = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo non trovato: " + id));
        if (!gymUsername.equals(t.getGymUsername())) {
            throw new AccessDeniedException("Il torneo appartiene a un altro gym");
        }
        return t;
    }

    private void requireStatus(Tournament t, String expected, String action) {
        if (!expected.equals(t.getStatus())) {
            throw new IllegalStateException("Impossibile " + action + " un torneo in stato '"
                    + t.getStatus() + "' (richiesto: '" + expected + "')");
        }
    }

    private void validateDates(TournamentRequest request) {
        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La data di inizio è nel passato");
        }
    }

    private TournamentDTO toDTO(Tournament tournament) {
        List<ParticipantDTO> participants = tournament.getParticipants().stream().map(this::toDTO).toList();

        return new TournamentDTO(
                tournament.getId(),
                tournament.getName(),
                tournament.getGymUsername(),
                tournament.getGymShopName(),
                tournament.getStartDate(),
                tournament.getStatus(),
                tournament.getLeaguePointRewardFirst(),
                tournament.getLeaguePointRequest(),
                tournament.getLimitParticipants(),
                participants,
                tournament.getLimitParticipants() != null
                        && participants.size() >= tournament.getLimitParticipants());
    }

    private ParticipantDTO toDTO(Participant participant) {
        return new ParticipantDTO(
                participant.getUsername(),
                participant.getDeckId(),
                participant.getArchetypeName(),
                participant.getFinalStanding(),
                participant.getLeaguePointEarned());
    }
}
