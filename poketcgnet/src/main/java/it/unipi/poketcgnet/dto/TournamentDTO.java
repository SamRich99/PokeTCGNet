package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentDTO {

    private String id;
    private String name;
    private String gymUsername;
    private String gymShopName;
    private LocalDate startDate;
    private String status;
    private Integer leaguePointRewardFirst;
    private Integer leaguePointRequest;
    private Integer limitParticipants;
    private List<ParticipantDTO> participants;
    // Calcolato (participants >= limit): il segnale per il gym che i posti sono
    // finiti
    private Boolean registrationFull;
}
