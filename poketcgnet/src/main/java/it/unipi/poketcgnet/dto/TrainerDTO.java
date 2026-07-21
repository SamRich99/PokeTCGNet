package it.unipi.poketcgnet.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TrainerDTO extends UserDTO {

    private String prefecture;
    private Integer leaguePoints;
    private Integer currentSeasonPoints;
    private Integer bestSeasonPoints;
    private Boolean isPro;
    private List<TournamentHistoryEntryDTO> tournamentHistory;
}
