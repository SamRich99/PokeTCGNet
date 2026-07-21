package it.unipi.poketcgnet.model.neo4j;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

// Replica leggera del Gym sul grafo. Come TrainerNode: username = @Id assegnato,
// lo stesso _id del documento Mongo.
@Node("Gym")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GymNode {

    @Id
    private String username;

    private String shopName;
    private String prefecture;
}
