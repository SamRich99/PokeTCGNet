package it.unipi.poketcgnet.model.neo4j;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.LocalDate;

// Replica leggera del Tournament sul grafo.
@Node("Tournament")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentNode {

    @Id
    private String id;

    private String name;
    private LocalDate startDate;
    private String status;
}
