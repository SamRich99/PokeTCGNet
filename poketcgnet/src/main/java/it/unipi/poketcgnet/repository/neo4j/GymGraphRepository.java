package it.unipi.poketcgnet.repository.neo4j;

import it.unipi.poketcgnet.dto.LoyalTrainersDTO;
import it.unipi.poketcgnet.model.neo4j.GymNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface GymGraphRepository extends Neo4jRepository<GymNode, String> {

        // Nodo Tournament + arco ORGANIZES in un colpo solo: l'arco parte dal Gym,
        @Query("""
                        MATCH (g:Gym {username: $gym})
                        MERGE (g)-[:ORGANIZES]->(t:Tournament {id: $id})
                        SET t.name = $name, t.startDate = $startDate, t.status = $status
                        """)
        void createTournament(@Param("gym") String gym, @Param("id") String id,
                        @Param("name") String name, @Param("startDate") LocalDate startDate,
                        @Param("status") String status);

        // trainer fedeli della gym Radicata sul Gym invece che su un Trainer
        @Query("""
                        MATCH (g:Gym {username: $gym})-[:ORGANIZES]->(t:Tournament)<-[:PARTICIPATES_IN]-(fedele:Trainer)
                        WHERE (fedele)-[:FOLLOWS]->(g)
                        WITH g, fedele, collect(DISTINCT t) AS torneiFedele
                        MATCH (fedele)-[:FOLLOWS]->(amico:Trainer)-[:FOLLOWS]->(fedele)
                        WHERE (amico)-[:FOLLOWS]->(g) AND fedele.username < amico.username
                        MATCH (g)-[:ORGANIZES]->(t2:Tournament)<-[:PARTICIPATES_IN]-(amico)
                        WHERE t2 IN torneiFedele
                        WITH fedele, amico, count(t2) AS torneiCondivisi
                        RETURN fedele.username AS trainerA, amico.username AS trainerB,
                               torneiCondivisi AS sharedTournaments
                        ORDER BY sharedTournaments DESC
                        LIMIT 20
                        """)
        List<LoyalTrainersDTO> findLoyalTrainers(@Param("gym") String gym);
}
