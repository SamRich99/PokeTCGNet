package it.unipi.poketcgnet.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Participant {

    private String userId;
    private String username;
    private String email;
    private String deckId;
    private String archetypeName;
    private Integer finalStanding;
    private Integer leaguePointEarned;
}
