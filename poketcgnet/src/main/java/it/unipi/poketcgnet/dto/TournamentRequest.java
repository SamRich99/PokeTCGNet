package it.unipi.poketcgnet.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// Body di POST /api/tournaments e PUT /api/tournaments/{id}. 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentRequest {

    @NotBlank
    private String name;

    @NotNull
    private LocalDate startDate;

    @NotNull
    @Min(2)
    private Integer limitParticipants;

    @NotNull
    @Min(0)
    private Integer leaguePointRequest;
}
