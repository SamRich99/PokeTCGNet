package it.unipi.poketcgnet.security;

import it.unipi.poketcgnet.model.mongo.Account;
import it.unipi.poketcgnet.repository.mongo.AdminRepository;
import it.unipi.poketcgnet.repository.mongo.GymRepository;
import it.unipi.poketcgnet.repository.mongo.TrainerRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AccountResolver {

    private final TrainerRepository trainerRepository;
    private final GymRepository gymRepository;
    private final AdminRepository adminRepository;

    public AccountResolver(TrainerRepository trainerRepository, GymRepository gymRepository,
            AdminRepository adminRepository) {
        this.trainerRepository = trainerRepository;
        this.gymRepository = gymRepository;
        this.adminRepository = adminRepository;
    }

    public record Resolved(Account account, String role) {
    }

    public Optional<Resolved> findByUsername(String username) {
        Optional<Resolved> trainer = trainerRepository.findByUsername(username)
                .map(t -> new Resolved(t, "TRAINER"));
        if (trainer.isPresent()) {
            return trainer;
        }
        Optional<Resolved> gym = gymRepository.findByUsername(username)
                .map(g -> new Resolved(g, "GYM"));
        if (gym.isPresent()) {
            return gym;
        }
        return adminRepository.findByUsername(username)
                .map(a -> new Resolved(a, "ADMIN"));
    }
}
