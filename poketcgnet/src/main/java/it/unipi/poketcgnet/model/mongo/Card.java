package it.unipi.poketcgnet.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    @Id
    private String id;

    private String name;
    private String cardCategory;
    private String pokemonType;
    private String set;
    private String setName;
    private String number;
    private String rarity;
    private String imageUrl;
}
