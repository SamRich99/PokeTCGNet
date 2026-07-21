package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Risultato query "trainer fedeli della gym"
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoyalTrainersDTO {

    private String trainerA;
    private String trainerB;
    private Long sharedTournaments;
}
