package it.unipi.poketcgnet.repository.mongo;

import it.unipi.poketcgnet.model.mongo.Archetype;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ArchetypeRepository extends MongoRepository<Archetype, String> {

    List<Archetype> findByName(String name);
    List<Archetype> findByCoreCards_CardIdIn(List<String> cardIds);

}
