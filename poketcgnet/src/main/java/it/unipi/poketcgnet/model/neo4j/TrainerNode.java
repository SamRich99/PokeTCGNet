package it.unipi.poketcgnet.model.neo4j;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

// Replica leggera del Trainer sul grafo
@Node("Trainer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerNode {

    @Id
    private String username;

    private String name;
    private String prefecture;
    private Boolean isPro;
}
