package it.unipi.poketcgnet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Body di POST /api/cards e PUT /api/cards/{id} 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardRequest {

    private String id;

    @NotBlank
    private String name;

    @NotBlank
    private String cardCategory; // Pokémon | Trainer | Energy

    private String pokemonType;

    @NotBlank
    private String set;

    private String setName;

    @NotBlank
    private String number;

    private String rarity;

    private String imageUrl;
}
