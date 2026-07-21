package it.unipi.poketcgnet.repository.mongo;

import it.unipi.poketcgnet.model.mongo.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

// Collezione "gyms" condivisa con Gym 
public interface AdminRepository extends MongoRepository<Admin, String> {

    @Query("{ 'username': ?0, 'role': 'admin' }")
    Optional<Admin> findByUsername(String username);

    @Query(value = "{ 'username': ?0, 'role': 'admin' }", exists = true)
    boolean existsByUsername(String username);

    @Query("{ 'email': ?0, 'role': 'admin' }")
    Optional<Admin> findByEmail(String email);
}
