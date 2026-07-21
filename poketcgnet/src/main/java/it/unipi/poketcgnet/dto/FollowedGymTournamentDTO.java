package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// Riga di risultato "tornei in registration dei gym seguiti"
// costruito direttamente dalla proiezione Cypher, senza tornare su
// Mongo per il documento completo
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowedGymTournamentDTO {

    private String tournamentId;
    private String name;
    private LocalDate startDate;
    private String gymName;
}
