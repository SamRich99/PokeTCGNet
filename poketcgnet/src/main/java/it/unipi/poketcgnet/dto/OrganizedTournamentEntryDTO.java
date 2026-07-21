package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizedTournamentEntryDTO {

    private String tournamentId;
    private String name;
    private LocalDate date;
    private Integer players;
    private String status;
}
