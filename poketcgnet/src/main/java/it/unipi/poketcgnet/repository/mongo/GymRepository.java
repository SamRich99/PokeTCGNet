package it.unipi.poketcgnet.repository.mongo;

import it.unipi.poketcgnet.dto.GymRatingDTO;
import it.unipi.poketcgnet.model.mongo.Gym;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

// Collezione "gyms" condivisa con Admin
public interface GymRepository extends MongoRepository<Gym, String> {

    @Query("{ 'role': 'gym' }")
    Slice<Gym> findAllBy(Pageable pageable);

    @Query("{ 'username': ?0, 'role': 'gym' }")
    Optional<Gym> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<Gym> findByEmail(String email);

    @Query("{ 'prefecture': ?0, 'role': 'gym' }")
    List<Gym> findByPrefecture(String prefecture);

    @Query("{ 'role': 'gym', $or: [ { username: { $regex: ?0, $options: 'i' } }, { name: { $regex: ?0, $options: 'i' } } ] }")
    List<Gym> searchGyms(String quotedPattern, Pageable pageable);

    // Aggregation "gym per prefettura ordinate per voto medio"
    @Aggregation(pipeline = {
            "{ $match: { role: 'gym', prefecture: ?0 } }",
            "{ $addFields: { reviewCount: { $size: { $ifNull: ['$reviews', []] } } } }",
            "{ $addFields: { avgRating: { $cond: [ { $gt: ['$reviewCount', 0] }, { $round: [ { $avg: '$reviews.score' }, 2 ] }, null ] } } }",
            "{ $sort: { avgRating: -1 } }",
            "{ $project: { _id: 0, username: 1, shopName: 1, shopAddress: 1, prefecture: 1, avgRating: 1, reviewCount: 1 } }"
    })
    List<GymRatingDTO> aggregateGymsByPrefectureRankedByRating(String prefecture);
}
