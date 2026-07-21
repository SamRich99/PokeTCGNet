package it.unipi.poketcgnet.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Body di POST /api/archetypes e PUT /api/archetypes/{id} (solo ADMIN).
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArchetypeRequest {

    @NotBlank
    private String name;

    @Valid
    private List<CoreCardRequest> coreCards;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoreCardRequest {

        @NotBlank
        private String cardId;

        @NotNull
        private Double presence; // frazione 0..1 dei deck dell'archetipo con la carta
    }
}
