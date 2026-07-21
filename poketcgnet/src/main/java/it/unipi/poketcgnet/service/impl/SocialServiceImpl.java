package it.unipi.poketcgnet.service.impl;

import it.unipi.poketcgnet.dto.CommunityDTO;
import it.unipi.poketcgnet.dto.FollowStatusDTO;
import it.unipi.poketcgnet.dto.LoyalTrainersDTO;
import it.unipi.poketcgnet.dto.ProTournamentDTO;
import it.unipi.poketcgnet.dto.RecommendedTournamentDTO;
import it.unipi.poketcgnet.dto.RecommendedTrainerDTO;
import it.unipi.poketcgnet.model.neo4j.GymNode;
import it.unipi.poketcgnet.model.neo4j.TrainerNode;
import it.unipi.poketcgnet.repository.neo4j.GymGraphRepository;
import it.unipi.poketcgnet.repository.neo4j.TrainerGraphRepository;
import it.unipi.poketcgnet.service.SocialService;
import it.unipi.poketcgnet.service.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SocialServiceImpl implements SocialService {

    private final TrainerGraphRepository trainerGraphRepository;
    private final GymGraphRepository gymGraphRepository;

    public SocialServiceImpl(TrainerGraphRepository trainerGraphRepository, GymGraphRepository gymGraphRepository) {
        this.trainerGraphRepository = trainerGraphRepository;
        this.gymGraphRepository = gymGraphRepository;
    }

    @Override
    public FollowStatusDTO followTrainer(String follower, String target) {
        checkTrainerExists(target);
        if (follower.equals(target)) {
            throw new IllegalArgumentException("Un trainer non può seguire se stesso");
        }
        trainerGraphRepository.followTrainer(follower, target);
        return new FollowStatusDTO(follower, target, true);
    }

    @Override
    public FollowStatusDTO unfollowTrainer(String follower, String target) {
        checkTrainerExists(target);
        trainerGraphRepository.unfollowTrainer(follower, target);
        return new FollowStatusDTO(follower, target, false);
    }

    @Override
    public FollowStatusDTO followGym(String follower, String target) {
        checkGymExists(target);
        trainerGraphRepository.followGym(follower, target);
        return new FollowStatusDTO(follower, target, true);
    }

    @Override
    public FollowStatusDTO unfollowGym(String follower, String target) {
        checkGymExists(target);
        trainerGraphRepository.unfollowGym(follower, target);
        return new FollowStatusDTO(follower, target, false);
    }

    @Override
    public List<TrainerNode> getFollowingTrainers(String username) {
        return trainerGraphRepository.findFollowingTrainers(username);
    }

    @Override
    public List<GymNode> getFollowingGyms(String username) {
        return trainerGraphRepository.findFollowingGyms(username);
    }

    @Override
    public List<RecommendedTrainerDTO> getRecommendedTrainers(String username) {
        return trainerGraphRepository.findRecommendedTrainers(username);
    }

    @Override
    public List<RecommendedTournamentDTO> getRecommendedTournaments(String username) {
        return trainerGraphRepository.findRecommendedTournaments(username);
    }

    @Override
    public List<ProTournamentDTO> getProTournaments(String username) {
        return trainerGraphRepository.findProTournaments(username);
    }

    @Override
    public List<CommunityDTO> getCommunity(String username) {
        return trainerGraphRepository.findCommunity(username);
    }

    @Override
    public List<LoyalTrainersDTO> getLoyalTrainers(String gymUsername) {
        checkGymExists(gymUsername);
        return gymGraphRepository.findLoyalTrainers(gymUsername);
    }

    private void checkTrainerExists(String username) {
        if (!trainerGraphRepository.existsById(username)) {
            throw new ResourceNotFoundException("Trainer non trovato: " + username);
        }
    }

    private void checkGymExists(String username) {
        if (!gymGraphRepository.existsById(username)) {
            throw new ResourceNotFoundException("Gym non trovato: " + username);
        }
    }
}
