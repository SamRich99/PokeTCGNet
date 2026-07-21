package it.unipi.poketcgnet.repository.neo4j;

import it.unipi.poketcgnet.dto.CommunityDTO;
import it.unipi.poketcgnet.dto.FollowedGymTournamentDTO;
import it.unipi.poketcgnet.dto.ProTournamentDTO;
import it.unipi.poketcgnet.dto.RecommendedTournamentDTO;
import it.unipi.poketcgnet.dto.RecommendedTrainerDTO;
import it.unipi.poketcgnet.model.neo4j.GymNode;
import it.unipi.poketcgnet.model.neo4j.TrainerNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrainerGraphRepository extends Neo4jRepository<TrainerNode, String> {

       @Query("""
                     MATCH (a:Trainer {username: $follower}), (b:Trainer {username: $target})
                     MERGE (a)-[:FOLLOWS]->(b)
                     """)
       void followTrainer(@Param("follower") String follower, @Param("target") String target);

       @Query("""
                     MATCH (a:Trainer {username: $follower})-[r:FOLLOWS]->(b:Trainer {username: $target})
                     DELETE r
                     """)
       void unfollowTrainer(@Param("follower") String follower, @Param("target") String target);

       @Query("""
                     MATCH (a:Trainer {username: $follower}), (b:Gym {username: $target})
                     MERGE (a)-[:FOLLOWS]->(b)
                     """)
       void followGym(@Param("follower") String follower, @Param("target") String target);

       @Query("""
                     MATCH (a:Trainer {username: $follower})-[r:FOLLOWS]->(b:Gym {username: $target})
                     DELETE r
                     """)
       void unfollowGym(@Param("follower") String follower, @Param("target") String target);

       @Query("""
                     MATCH (:Trainer {username: $username})-[:FOLLOWS]->(t:Trainer)
                     RETURN t
                     """)
       List<TrainerNode> findFollowingTrainers(@Param("username") String username);

       @Query("""
                     MATCH (:Trainer {username: $username})-[:FOLLOWS]->(g:Gym)
                     RETURN g.username AS username, g.shopName AS shopName, g.prefecture AS prefecture
                     """)
       List<GymNode> findFollowingGyms(@Param("username") String username);

       // Tournaments di gym seguiti
       @Query("""
                     MATCH (:Trainer {username: $me})-[:FOLLOWS]->(g:Gym)-[:ORGANIZES]->(t:Tournament {status: 'registration'})
                     RETURN t.id AS tournamentId, t.name AS name, t.startDate AS startDate, g.shopName AS gymName
                     ORDER BY t.startDate ASC
                     """)
       List<FollowedGymTournamentDTO> findRegistrationTournamentsFromFollowedGyms(@Param("me") String me);

       @Query("""
                     MATCH (a:Trainer {username: $trainer}), (t:Tournament {id: $tournament})
                     MERGE (a)-[:PARTICIPATES_IN]->(t)
                     """)
       void participateInTournament(@Param("trainer") String trainer, @Param("tournament") String tournament);

       @Query("""
                     MATCH (a:Trainer {username: $trainer})-[r:PARTICIPATES_IN]->(:Tournament {id: $tournament})
                     DELETE r
                     """)
       void leaveTournament(@Param("trainer") String trainer, @Param("tournament") String tournament);

       // Pro Tournament
       @Query("""
                     MATCH (me:Trainer {username: $me})-[:PARTICIPATES_IN]->(t:Tournament)<-[:PARTICIPATES_IN]-(other:Trainer)
                     WHERE other <> me AND NOT (me)-[:FOLLOWS]->(other)
                     WITH me, other, count(DISTINCT t) AS torneiInComune
                     OPTIONAL MATCH (me)-[:FOLLOWS]->(g:Gym)<-[:FOLLOWS]-(other)
                     WITH me, other, torneiInComune, count(DISTINCT g) AS negoziInComune
                     OPTIONAL MATCH (me)-[:FOLLOWS]->(x:Trainer)-[:FOLLOWS]->(other)
                     WITH me, other, torneiInComune, negoziInComune, count(DISTINCT x) AS amiciInComune
                     RETURN other.username AS username, other.name AS name,
                            torneiInComune AS sharedTournaments, negoziInComune AS sharedGyms, amiciInComune AS sharedFriends,
                            (other.prefecture = me.prefecture) AS samePrefecture,
                            round(4 * sqrt(torneiInComune)
                                + 3 * sqrt(negoziInComune)
                                + 2 * sqrt(amiciInComune)
                                + (CASE WHEN other.prefecture = me.prefecture THEN 3 ELSE 0 END), 2) AS score
                     ORDER BY score DESC
                     LIMIT 10
                     """)
       List<RecommendedTrainerDTO> findRecommendedTrainers(@Param("me") String me);

       // tornei raccomandati in base alla rete sociale
       @Query("""
                     MATCH (me:Trainer {username: $me})
                     OPTIONAL MATCH (me)-[:FOLLOWS]->(amico:Trainer)-[:FOLLOWS]->(me)
                     WITH me, collect(DISTINCT amico) AS amici
                     OPTIONAL MATCH (me)-[:FOLLOWS]->(:Trainer)-[:FOLLOWS]->(fof:Trainer)
                     WHERE fof <> me
                     WITH me, amici, [x IN collect(DISTINCT fof) WHERE NOT x IN amici] AS amiciDiAmici
                     MATCH (g:Gym)-[:ORGANIZES]->(t:Tournament {status: 'registration'})
                     WHERE NOT (me)-[:PARTICIPATES_IN]->(t)
                     MATCH (p:Trainer)-[:PARTICIPATES_IN]->(t)
                     WITH me, g, t, amici, amiciDiAmici, collect(p) AS iscritti
                     WITH t, g,
                          EXISTS { (me)-[:FOLLOWS]->(g) } AS seguoIlNegozio,
                          (g.prefecture = me.prefecture) AS inZona,
                          size([x IN iscritti WHERE x IN amici]) AS amiciIscritti,
                          size([x IN iscritti WHERE x IN amiciDiAmici]) AS amiciDiAmiciIscritti
                     RETURN t.id AS tournamentId, t.name AS name, t.startDate AS startDate,
                            g.shopName AS gymName, seguoIlNegozio AS followedGym, inZona AS sameArea,
                            amiciIscritti AS friendsRegistered, amiciDiAmiciIscritti AS friendsOfFriendsRegistered,
                            round((CASE WHEN seguoIlNegozio THEN 8 ELSE 0 END)
                                + (CASE WHEN inZona THEN 3 ELSE 0 END)
                                + 3 * sqrt(amiciIscritti)
                                + 2 * sqrt(amiciDiAmiciIscritti), 2) AS score
                     ORDER BY score DESC, startDate
                     LIMIT 10
                     """)
       List<RecommendedTournamentDTO> findRecommendedTournaments(@Param("me") String me);

       // tornei pro. Attraversamento FOLLOWS a profondita' variabile (1..3)
       @Query("""
                     MATCH (me:Trainer {username: $me})-[:FOLLOWS*1..3]->(pro:Trainer)
                     WHERE pro.isPro AND pro <> me
                     MATCH (g:Gym)-[:ORGANIZES]->(t:Tournament {status: 'registration'})
                     WHERE (pro)-[:PARTICIPATES_IN]->(t) AND NOT (me)-[:PARTICIPATES_IN]->(t)
                     RETURN t.id AS tournamentId, t.name AS name, t.startDate AS startDate,
                            g.shopName AS gymName,
                            (g.prefecture = me.prefecture) AS sameArea,
                            collect(DISTINCT pro.username)[..5] AS proTrainers
                     ORDER BY sameArea DESC, startDate
                     LIMIT 10
                     """)
       List<ProTournamentDTO> findProTournaments(@Param("me") String me);

       // Community personale. Amici (follow reciproco) che seguono lo stesso
       // negozio e hanno giocato almeno un torneo insieme, raggruppati per negozio.
       @Query("""
                     MATCH (me:Trainer {username: $me})-[:FOLLOWS]->(amico:Trainer)-[:FOLLOWS]->(me)
                     MATCH (me)-[:FOLLOWS]->(g:Gym)<-[:FOLLOWS]-(amico)
                     MATCH (me)-[:PARTICIPATES_IN]->(t:Tournament)<-[:PARTICIPATES_IN]-(amico)
                     RETURN g.shopName AS gymName,
                            collect(DISTINCT amico.username) AS members,
                            count(DISTINCT t) AS sharedTournaments
                     ORDER BY size(members) DESC
                     """)
       List<CommunityDTO> findCommunity(@Param("me") String me);

       // Fine stagione (admin): azzera isPro su tutti i nodi Trainer
       @Query("MATCH (t:Trainer) SET t.isPro = false")
       void resetAllIsPro();

       // Promuove a pro i soli username indicati
       @Query("""
                     UNWIND $usernames AS u
                     MATCH (t:Trainer {username: u})
                     SET t.isPro = true
                     """)
       void promoteToPro(@Param("usernames") List<String> usernames);
}
