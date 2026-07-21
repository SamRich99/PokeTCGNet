package it.unipi.poketcgnet.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Body di POST /api/decks e PUT /api/decks/{id}. Il proprietario NON è nel body:
// è sempre il principal autenticato. 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeckRequest {

    @NotBlank
    private String name;

    @NotEmpty
    @Valid
    private List<DeckCardRequest> cards;

    private String archetypeId;
    private String archetypeName;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeckCardRequest {

        @NotBlank
        private String cardId;

        @NotNull
        @Min(1)
        private Integer quantity;
    }
}
