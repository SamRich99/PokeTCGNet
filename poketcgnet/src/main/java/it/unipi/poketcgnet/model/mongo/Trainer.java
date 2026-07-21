package it.unipi.poketcgnet.model.mongo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "trainers")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Trainer extends Account {

    private String prefecture;
    private Integer leaguePoints;
    private Integer currentSeasonPoints;
    private Integer bestSeasonPoints;
    private Boolean isPro;
    private List<TournamentHistoryEntry> tournamentHistory;
}
