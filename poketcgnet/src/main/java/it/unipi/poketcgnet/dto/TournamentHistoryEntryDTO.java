package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentHistoryEntryDTO {

    private String tournamentId;
    private String tournamentName;
    private LocalDate date;
    private String archetypeName;
    private String deckId;
    private Integer finalStanding;
    private Integer leaguePointEarned;
}
