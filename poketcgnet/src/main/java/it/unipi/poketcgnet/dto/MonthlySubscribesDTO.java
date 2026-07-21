package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Risultato aggregation "iscrizioni per mese" 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySubscribesDTO {

    private String month; // "yyyy-MM"
    private Integer newSubscribes;
    private Integer deltaVsPrevMonth;
}
