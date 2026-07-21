package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

// Riga di risultato della query "tornei pro"
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProTournamentDTO {

    private String tournamentId;
    private String name;
    private LocalDate startDate;
    private String gymName;
    private Boolean sameArea;
    private List<String> proTrainers;
}
