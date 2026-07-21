package it.unipi.poketcgnet.model.mongo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

// Collezione "gyms" condivisa con Admin 
@Document(collection = "gyms")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Gym extends Account {

    private String role;
    private String piva;
    private String shopName;
    private String shopAddress;
    private String prefecture;
    private List<Review> reviews;
    private List<OrganizedTournamentEntry> organizedTournaments;
}
