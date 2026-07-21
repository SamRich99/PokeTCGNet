package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// Riga di risultato della query "tornei raccomandati" 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedTournamentDTO {

    private String tournamentId;
    private String name;
    private LocalDate startDate;
    private String gymName;
    private Boolean followedGym;
    private Boolean sameArea;
    private Long friendsRegistered;
    private Long friendsOfFriendsRegistered;
    private Double score;
}
