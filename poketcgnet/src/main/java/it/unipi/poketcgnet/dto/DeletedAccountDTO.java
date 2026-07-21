package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Risposta di DELETE /api/trainers/me, /api/gyms/me e delle rispettive moderazioni
// ADMIN su /{username}. 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeletedAccountDTO {

    private String username;
    private List<DeletedTournamentDTO> cancelledTournaments;
}
