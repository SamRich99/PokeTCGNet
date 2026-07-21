package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Risposta di PUT /api/tournaments/{id}. Oltre al torneo aggiornato, participantEmails
// contiene le mail di chi era già iscritto (vuota se nessuno si era ancora registrato)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatedTournamentDTO {

    private TournamentDTO tournament;
    private List<String> participantEmails;
}
