package it.unipi.poketcgnet.service;

import it.unipi.poketcgnet.dto.CommunityDTO;
import it.unipi.poketcgnet.dto.FollowStatusDTO;
import it.unipi.poketcgnet.dto.LoyalTrainersDTO;
import it.unipi.poketcgnet.dto.ProTournamentDTO;
import it.unipi.poketcgnet.dto.RecommendedTournamentDTO;
import it.unipi.poketcgnet.dto.RecommendedTrainerDTO;
import it.unipi.poketcgnet.model.neo4j.GymNode;
import it.unipi.poketcgnet.model.neo4j.TrainerNode;

import java.util.List;

public interface SocialService {

    FollowStatusDTO followTrainer(String follower, String target);

    FollowStatusDTO unfollowTrainer(String follower, String target);

    FollowStatusDTO followGym(String follower, String target);

    FollowStatusDTO unfollowGym(String follower, String target);

    List<TrainerNode> getFollowingTrainers(String username);

    List<GymNode> getFollowingGyms(String username);

    List<RecommendedTrainerDTO> getRecommendedTrainers(String username);

    List<RecommendedTournamentDTO> getRecommendedTournaments(String username);

    List<ProTournamentDTO> getProTournaments(String username);

    List<CommunityDTO> getCommunity(String username);

    List<LoyalTrainersDTO> getLoyalTrainers(String gymUsername);
}
