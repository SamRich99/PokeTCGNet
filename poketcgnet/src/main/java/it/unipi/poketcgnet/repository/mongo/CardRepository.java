package it.unipi.poketcgnet.repository.mongo;

import it.unipi.poketcgnet.model.mongo.Card;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CardRepository extends MongoRepository<Card, String> {

    Slice<Card> findAllBy(Pageable pageable);

    List<Card> findByPokemonType(String pokemonType);

    List<Card> findByCardCategory(String cardCategory);

    List<Card> findByName(String name);
}
