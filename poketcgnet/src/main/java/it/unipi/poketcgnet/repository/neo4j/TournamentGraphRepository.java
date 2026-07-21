package it.unipi.poketcgnet.repository.neo4j;

import it.unipi.poketcgnet.model.neo4j.TournamentNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

public interface TournamentGraphRepository extends Neo4jRepository<TournamentNode, String> {

    @Query("""
            MATCH (t:Tournament {id: $id})
            SET t.status = $status
            """)
    void updateStatus(@Param("id") String id, @Param("status") String status);
}
