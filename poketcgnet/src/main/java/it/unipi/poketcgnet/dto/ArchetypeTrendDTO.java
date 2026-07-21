package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Risultato aggregation "trend homepage", archetipi più giocati nei tornei
// conclusi dell'ultimo periodo, ciascuno col deck che ha totalizzato più punti.
// playratePercent = deck distinti di quell'archetipo / deck distinti totali
// giocati nello stesso periodo.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArchetypeTrendDTO {

    private String archetypeName;
    private Integer nDecks;
    private Integer totalPoints;
    private Double playratePercent;
    private BestDeckDTO bestDeck;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BestDeckDTO {
        private String deckId;
        private String owner;
        private Integer points;
    }
}
