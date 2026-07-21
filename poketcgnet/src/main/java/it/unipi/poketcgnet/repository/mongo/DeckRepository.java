package it.unipi.poketcgnet.repository.mongo;

import it.unipi.poketcgnet.model.mongo.Deck;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface DeckRepository extends MongoRepository<Deck, String> {

    // Le liste mostrano solo i deck "vivi": il flag deleted marca le versioni
    // eliminate dal trainer o superate da una modifica (copy-on-write).
    @Query("{ deleted: { $ne: true } }")
    Slice<Deck> findAllBy(Pageable pageable);

    @Query("{ ownerId: ?0, deleted: { $ne: true } }")
    List<Deck> findByOwnerId(String ownerId);

    @Query(value = "{ archetypeId: ?0, deleted: { $ne: true } }", sort = "{ competitivePoint: -1 }")
    List<Deck> findByArchetypeIdOrderByCompetitivePointDesc(String archetypeId);
}
