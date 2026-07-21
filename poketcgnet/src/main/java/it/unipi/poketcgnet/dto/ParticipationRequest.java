package it.unipi.poketcgnet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Body di POST /api/tournaments/{id}/participants
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequest {

    @NotBlank
    private String deckId;
}
