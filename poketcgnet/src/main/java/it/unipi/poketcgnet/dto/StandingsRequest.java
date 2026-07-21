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

// Body di PUT /api/tournaments/{id}/standings: la classifica finale inserita dal
// gym. Deve coprire esattamente i participants del torneo, con piazzamenti 1..n
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandingsRequest {

    @NotEmpty
    @Valid
    private List<StandingEntry> standings;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StandingEntry {

        @NotBlank
        private String username;

        @NotNull
        @Min(1)
        private Integer finalStanding;
    }
}
