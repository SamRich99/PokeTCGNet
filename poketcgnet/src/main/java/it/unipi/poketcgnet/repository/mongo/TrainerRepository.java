package it.unipi.poketcgnet.repository.mongo;

import it.unipi.poketcgnet.dto.MonthlySubscribesDTO;
import it.unipi.poketcgnet.dto.TrainerKpiDTO;
import it.unipi.poketcgnet.model.mongo.Trainer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TrainerRepository extends MongoRepository<Trainer, String> {

        Slice<Trainer> findAllBy(Pageable pageable);

        Optional<Trainer> findByUsername(String username);

        boolean existsByUsername(String username);

        Optional<Trainer> findByEmail(String email);

        long countByCurrentSeasonPointsGreaterThan(int value);

        List<Trainer> findByCurrentSeasonPointsGreaterThanEqual(int value);

        @Query("{ $or: [ { username: { $regex: ?0, $options: 'i' } }, { name: { $regex: ?0, $options: 'i' } } ] }")
        List<Trainer> searchTrainers(String quotedPattern, Pageable pageable);

        // iscrizioni trainer per mese (analytics admin)
        @Aggregation(pipeline = {
                        "{ $match: { createdAt: { $gte: ?0 } } }",
                        "{ $group: { _id: { $substrBytes: ['$createdAt', 0, 7] }, newSubscribes: { $sum: 1 } } }",
                        "{ $project: { _id: 0, month: '$_id', newSubscribes: 1 } }"
        })
        List<MonthlySubscribesDTO> aggregateSubscribesSince(String from);

        // per ogni archetipo giocato dal trainer, partecipazioni, punti totali,
        // piazzamento medio e tasso di top3
        @Aggregation(pipeline = {
                        "{ $match: { username: ?0 } }",
                        "{ $unwind: '$tournamentHistory' }",
                        "{ $group: { " +
                                        "_id: '$tournamentHistory.archetypeName', " +
                                        "played: { $sum: 1 }, " +
                                        "totalPoints: { $sum: '$tournamentHistory.leaguePointEarned' }, " +
                                        "avgStanding: { $avg: '$tournamentHistory.finalStanding' }, " +
                                        "top3: { $sum: { $cond: [ { $lte: ['$tournamentHistory.finalStanding', 3] }, 1, 0 ] } } } }",
                        "{ $addFields: { top3Rate: { $round: [ { $multiply: [ { $divide: ['$top3', '$played'] }, 100 ] }, 1 ] } } }",
                        "{ $sort: { totalPoints: -1 } }",
                        "{ $project: { _id: 0, archetypeName: '$_id', played: 1, totalPoints: 1, avgStanding: 1, top3: 1, top3Rate: 1 } }"
        })
        List<TrainerKpiDTO> aggregateTrainerKpi(String username);
}
