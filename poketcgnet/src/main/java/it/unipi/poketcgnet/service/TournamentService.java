package it.unipi.poketcgnet.service;

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

import java.util.List;

public interface TournamentService {

    SliceDTO<TournamentDTO> getTournamentsPage(int pageNumber);

    TournamentDTO getTournamentById(String id);

    List<TournamentDTO> searchTournamentsByName(String name);

    // Homepage trainer tornei in registration dei gym che
    // il trainer autenticato segue
    List<FollowedGymTournamentDTO> getRegistrationTournamentsFromFollowedGyms(String trainerUsername);

    // Sezione Trend: i "limit" tornei globali giocati (concluded) con il maggior
    // leaguePointRequest
    List<TournamentDTO> getTrendTournaments(int limit);

    // analytics admin: tornei conclusi per mese con delta
    List<MonthlyTournamentsDTO> getMonthlyTournaments();

    TournamentDTO createTournament(String gymUsername, TournamentRequest request);

    UpdatedTournamentDTO updateTournament(String id, String gymUsername, TournamentRequest request);

    TournamentDTO closeRegistration(String id, String gymUsername);

    TournamentDTO publishStandings(String id, String gymUsername, StandingsRequest request);

    DeletedTournamentDTO deleteTournament(String id, String requesterUsername, boolean isAdmin);

    TournamentDTO joinTournament(String id, String trainerUsername, ParticipationRequest request);

    TournamentDTO leaveTournament(String id, String trainerUsername);

    // Rimozione di un iscritto da parte del gym (es. no-show): ammessa in
    // registration/elaboration, mai in concluded.
    TournamentDTO removeParticipant(String id, String gymUsername, String participantUsername);

    SliceDTO<TournamentDTO> searchTournamentsAdvanced(TournamentSearchFilter filter, int pageNumber);
}
