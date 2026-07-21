package it.unipi.poketcgnet.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizedTournamentEntry {

    private String tournamentId;
    private String name;
    private LocalDate date;
    private Integer players;
    // registration | elaboration | concluded — aggiornato ad ogni transizione di
    // stato del torneo
    private String status;
}
