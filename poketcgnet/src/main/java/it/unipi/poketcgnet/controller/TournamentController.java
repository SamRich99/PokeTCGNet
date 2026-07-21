package it.unipi.poketcgnet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.poketcgnet.dto.DeletedTournamentDTO;
import it.unipi.poketcgnet.dto.FollowedGymTournamentDTO;
import it.unipi.poketcgnet.dto.MonthlyTournamentsDTO;
import it.unipi.poketcgnet.dto.ParticipationRequest;
import it.unipi.poketcgnet.dto.SliceDTO;
import it.unipi.poketcgnet.dto.StandingsRequest;
import it.unipi.poketcgnet.dto.TournamentDTO;
import it.unipi.poketcgnet.dto.TournamentRequest;
import it.unipi.poketcgnet.dto.TournamentSearchFilter;
import it.unipi.poketcgnet.dto.UpdatedTournamentDTO;
import it.unipi.poketcgnet.service.TournamentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Tournaments", description = "Endpoints for managing the tournament lifecycle")
@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @Operation(summary = "Get a paginated list of tournaments")
    @GetMapping("/page/{pageNumber}")
    public SliceDTO<TournamentDTO> getTournamentsPage(@PathVariable int pageNumber) {
        return tournamentService.getTournamentsPage(pageNumber);
    }

    @Operation(summary = "Get a tournament's details by id")
    @GetMapping("/{id}")
    public TournamentDTO getTournamentById(@PathVariable String id) {
        return tournamentService.getTournamentById(id);
    }

    @Operation(summary = "Search tournaments by name")
    @GetMapping("/search")
    public List<TournamentDTO> searchByName(@RequestParam String name) {
        return tournamentService.searchTournamentsByName(name);
    }

    // Ricerca combinata multi-attributo
    @Operation(summary = "Search tournaments with multiple optional filters")
    @GetMapping("/filter")
    public SliceDTO<TournamentDTO> filterTournaments(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String gymUsername,
            @RequestParam(required = false) String prefecture,
            @RequestParam(required = false) LocalDate startDateFrom,
            @RequestParam(required = false) LocalDate startDateTo,
            @RequestParam(required = false) Integer minLeaguePointRequest,
            @RequestParam(required = false) Integer maxLeaguePointRequest,
            @RequestParam(required = false) Integer minLimitParticipants,
            @RequestParam(required = false) Integer maxLimitParticipants,
            @RequestParam(defaultValue = "1") int pageNumber) {
        return tournamentService.searchTournamentsAdvanced(new TournamentSearchFilter(
                name, status, gymUsername, prefecture,
                startDateFrom, startDateTo,
                minLeaguePointRequest, maxLeaguePointRequest,
                minLimitParticipants, maxLimitParticipants), pageNumber);
    }

    // Sezione Trend: i tornei globali giocati con il maggior leaguePointRequest
    @Operation(summary = "Get the top tournaments by league points requested")
    @GetMapping("/trend")
    public List<TournamentDTO> getTrendTournaments(@RequestParam(defaultValue = "10") int limit) {
        return tournamentService.getTrendTournaments(limit);
    }

    // analytics admin: tornei conclusi per mese con delta
    @Operation(summary = "Get monthly concluded-tournament statistics (admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/analytics/creations")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MonthlyTournamentsDTO> getMonthlyTournaments() {
        return tournamentService.getMonthlyTournaments();
    }

    @Operation(summary = "Get registration-phase tournaments from gyms the authenticated trainer follows")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/following")
    public List<FollowedGymTournamentDTO> getRegistrationTournamentsFromFollowedGyms(Authentication authentication) {
        return tournamentService.getRegistrationTournamentsFromFollowedGyms(authentication.getName());
    }

    @Operation(summary = "Create a new tournament")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TournamentDTO createTournament(@Valid @RequestBody TournamentRequest request,
            Authentication authentication) {
        return tournamentService.createTournament(authentication.getName(), request);
    }

    // Modificabile solo finché è in fase di registrazione
    @Operation(summary = "Update a tournament still in registration")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public UpdatedTournamentDTO updateTournament(@PathVariable String id,
            @Valid @RequestBody TournamentRequest request,
            Authentication authentication) {
        return tournamentService.updateTournament(id, authentication.getName(), request);
    }

    // registration -> elaboration (iscrizioni chiuse, in attesa di classifica)
    @Operation(summary = "Close registration and move a tournament to elaboration")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/close-registration")
    public TournamentDTO closeRegistration(@PathVariable String id, Authentication authentication) {
        return tournamentService.closeRegistration(id, authentication.getName());
    }

    // elaboration -> concluded: il gym pubblica la classifica; il server calcola i
    // punti e aggiorna storico/punteggi dei trainer e storico del gym
    @Operation(summary = "Publish final standings and conclude a tournament")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/standings")
    public TournamentDTO publishStandings(@PathVariable String id,
            @Valid @RequestBody StandingsRequest request,
            Authentication authentication) {
        return tournamentService.publishStandings(id, authentication.getName(), request);
    }

    // Iscrizione del trainer autenticato con un proprio deck; a limitParticipants
    // le iscrizioni si bloccano (richiesta rifiutata) ma lo stato resta
    // 'registration': la chiusura verso 'elaboration' e' sempre un atto manuale del gym
    @Operation(summary = "Register the authenticated trainer for a tournament")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/participants")
    @ResponseStatus(HttpStatus.CREATED)
    public TournamentDTO joinTournament(@PathVariable String id,
            @Valid @RequestBody ParticipationRequest request,
            Authentication authentication) {
        return tournamentService.joinTournament(id, authentication.getName(), request);
    }

    // Disiscrizione del trainer autenticato, consentita solo in fase di
    // registrazione
    @Operation(summary = "Withdraw the authenticated trainer from a tournament")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}/participants")
    public TournamentDTO leaveTournament(@PathVariable String id, Authentication authentication) {
        return tournamentService.leaveTournament(id, authentication.getName());
    }

    // Il gym rimuove un iscritto specifico (es. no-show), consentito anche a
    // iscrizioni già chiuse (elaboration), mai su un torneo concluso
    @Operation(summary = "Remove a participant from a tournament")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}/participants/{username}")
    public TournamentDTO removeParticipant(@PathVariable String id, @PathVariable String username,
            Authentication authentication) {
        return tournamentService.removeParticipant(id, authentication.getName(), username);
    }

    // Gym: annulla un torneo non concluso e riceve le email
    // degli iscritti per comunicare l'annullamento.
    // Admin: pulizia (anche conclusi, solo documento Mongo), nessuna email
    // restituita.
    @Operation(summary = "Delete a tournament")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public DeletedTournamentDTO deleteTournament(@PathVariable String id, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return tournamentService.deleteTournament(id, authentication.getName(), isAdmin);
    }
}
