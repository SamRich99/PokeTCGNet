package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Risultato aggregation "tornei conclusi per mese" 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTournamentsDTO {

    private String month; // "yyyy-MM"
    private Integer newTournaments;
    private Integer deltaVsPrevMonth;
}
