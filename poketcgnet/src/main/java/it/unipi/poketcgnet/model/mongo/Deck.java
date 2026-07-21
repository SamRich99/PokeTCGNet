package it.unipi.poketcgnet.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "decks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Deck {

    @Id
    private String id;
    private String name;
    private String ownerId;
    private String ownerUsername;
    private String archetypeId;
    private String archetypeName;
    private LocalDate createdAt;
    private Integer competitivePoint;
    private List<DeckCard> cards;
    // Soft delete/versioning: true se il trainer ha eliminato il deck o l'ha
    // sostituito con una nuova versione (copy-on-write). I deck flaggati spariscono
    // dalle liste ma restano leggibili per id dal drill-down dei tornei.
    private Boolean deleted;
}
