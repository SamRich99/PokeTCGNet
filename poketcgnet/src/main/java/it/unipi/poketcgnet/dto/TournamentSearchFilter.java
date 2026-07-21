package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// Filtro per GET /api/tournaments/filter: tutti i campi sono opzionali e componibili
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentSearchFilter {

    private String name;
    private String status;
    private String gymUsername;
    private String prefecture;
    private LocalDate startDateFrom;
    private LocalDate startDateTo;
    private Integer minLeaguePointRequest;
    private Integer maxLeaguePointRequest;
    private Integer minLimitParticipants;
    private Integer maxLimitParticipants;
}
