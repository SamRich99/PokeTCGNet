package it.unipi.poketcgnet.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "tournaments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tournament {

    @Id
    private String id;
    private String name;
    private String gymId;
    private String gymUsername;
    private String gymShopName;
    private LocalDate startDate;
    private String status;
    private Integer leaguePointRewardFirst;
    private Integer leaguePointRequest;
    private Integer limitParticipants;
    private List<Participant> participants;
}
