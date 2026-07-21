package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Risposta di DELETE /api/tournaments/{id}.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeletedTournamentDTO {

    private String tournamentId;
    private String name;
    private List<String> participantEmails;
}
