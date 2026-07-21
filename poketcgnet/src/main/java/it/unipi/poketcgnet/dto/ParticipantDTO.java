package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDTO {

    private String username;
    private String deckId;
    private String archetypeName;
    private Integer finalStanding;
    private Integer leaguePointEarned;
}
