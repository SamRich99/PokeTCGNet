package it.unipi.poketcgnet.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "archetypes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Archetype {

    @Id
    private String id;

    private String name;
    private List<CoreCard> coreCards;

}
