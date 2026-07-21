package it.unipi.poketcgnet.repository.mongo;

import it.unipi.poketcgnet.dto.ArchetypeTrendDTO;
import it.unipi.poketcgnet.dto.MonthlyTournamentsDTO;
import it.unipi.poketcgnet.model.mongo.Tournament;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TournamentRepository extends MongoRepository<Tournament, String> {

        Slice<Tournament> findAllBy(Pageable pageable);

        // Sezione Trend: i tornei globali con il maggior leaguePointRequest, tra quelli
        // già giocati (concluded)
        List<Tournament> findByStatusOrderByLeaguePointRequestDesc(String status, Pageable pageable);

        List<Tournament> findByGymIdOrderByStartDateAsc(String gymId);

        List<Tournament> findByNameContainingIgnoreCaseOrderByStartDateAsc(String name);

        List<Tournament> findByParticipants_UserIdOrderByStartDateAsc(String userId);

        // tornei conclusi per mese (analytics admin)
        @Aggregation(pipeline = {
                        "{ $match: { status: 'concluded', startDate: { $gte: ?0 } } }",
                        "{ $group: { _id: { $substrBytes: ['$startDate', 0, 7] }, newTournaments: { $sum: 1 } } }",
                        "{ $project: { _id: 0, month: '$_id', newTournaments: 1 } }"
        })
        List<MonthlyTournamentsDTO> aggregateTournamentsSince(String from);

        // archetipi più giocati nei tornei conclusi dal cutoff in poi
        @Aggregation(pipeline = {
                        "{ $match: { status: 'concluded', startDate: { $gte: ?0 } } }",
                        "{ $unwind: '$participants' }",
                        "{ $sort: { 'participants.leaguePointEarned': -1 } }",
                        "{ $group: { " +
                                        "_id: '$participants.archetypeName', " +
                                        "decks: { $addToSet: '$participants.deckId' }, " +
                                        "totalPoints: { $sum: '$participants.leaguePointEarned' }, " +
                                        "bestDeck: { $first: { deckId: '$participants.deckId', owner: '$participants.username', points: '$participants.leaguePointEarned' } } } }",
                        "{ $addFields: { nDecks: { $size: '$decks' } } }",
                        "{ $facet: { " +
                                        "archetypes: [ { $project: { _id: 0, archetypeName: '$_id', nDecks: 1, totalPoints: 1, bestDeck: 1 } } ], "
                                        +
                                        "total: [ { $group: { _id: null, sum: { $sum: '$nDecks' } } } ] } }",
                        "{ $unwind: '$archetypes' }",
                        "{ $addFields: { 'archetypes.playratePercent': { $round: [ { $multiply: [ { $divide: ['$archetypes.nDecks', { $arrayElemAt: ['$total.sum', 0] }] }, 100 ] }, 1 ] } } }",
                        "{ $replaceRoot: { newRoot: '$archetypes' } }",
                        "{ $sort: { totalPoints: -1 } }",
                        "{ $limit: 10 }"
        })
        List<ArchetypeTrendDTO> aggregateArchetypeTrends(String cutoffDate);
}
