package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO {

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
