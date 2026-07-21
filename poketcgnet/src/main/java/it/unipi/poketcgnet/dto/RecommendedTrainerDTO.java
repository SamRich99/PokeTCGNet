package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Riga di risultato della query "trainer raccomandati"
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedTrainerDTO {

    private String username;
    private String name;
    private Long sharedTournaments;
    private Long sharedGyms;
    private Long sharedFriends;
    private Boolean samePrefecture;
    private Double score;
}
