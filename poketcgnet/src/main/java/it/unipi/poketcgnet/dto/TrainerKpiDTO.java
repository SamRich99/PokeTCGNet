package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Risultato aggregation "KPI trainer" una riga per archetipo
// giocato dal trainer, ordinata per punti totali.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerKpiDTO {

    private String archetypeName;
    private Integer played;
    private Integer totalPoints;
    private Double avgStanding;
    private Integer top3;
    private Double top3Rate; // percentuale 0-100, un decimale
}
