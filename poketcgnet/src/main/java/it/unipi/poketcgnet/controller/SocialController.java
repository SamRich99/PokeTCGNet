package it.unipi.poketcgnet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.poketcgnet.dto.CommunityDTO;
import it.unipi.poketcgnet.dto.FollowStatusDTO;
import it.unipi.poketcgnet.dto.LoyalTrainersDTO;
import it.unipi.poketcgnet.dto.ProTournamentDTO;
import it.unipi.poketcgnet.dto.RecommendedTournamentDTO;
import it.unipi.poketcgnet.dto.RecommendedTrainerDTO;
import it.unipi.poketcgnet.model.neo4j.GymNode;
import it.unipi.poketcgnet.model.neo4j.TrainerNode;
import it.unipi.poketcgnet.security.UserPrincipal;
import it.unipi.poketcgnet.service.SocialService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Social", description = "Endpoints for the graph-based social features (Requires authentication)")
@RestController
@RequestMapping("/api/social")
@SecurityRequirement(name = "bearerAuth")
public class SocialController {

    private final SocialService socialService;

    public SocialController(SocialService socialService) {
        this.socialService = socialService;
    }

    @Operation(summary = "Follow another trainer")
    @PostMapping("/following/trainers/{username}")
    @ResponseStatus(HttpStatus.CREATED)
    public FollowStatusDTO followTrainer(@AuthenticationPrincipal UserPrincipal me, @PathVariable String username) {
        return socialService.followTrainer(me.getUsername(), username);
    }

    @Operation(summary = "Unfollow a trainer")
    @DeleteMapping("/following/trainers/{username}")
    public FollowStatusDTO unfollowTrainer(@AuthenticationPrincipal UserPrincipal me, @PathVariable String username) {
        return socialService.unfollowTrainer(me.getUsername(), username);
    }

    @Operation(summary = "Follow a gym")
    @PostMapping("/following/gyms/{username}")
    @ResponseStatus(HttpStatus.CREATED)
    public FollowStatusDTO followGym(@AuthenticationPrincipal UserPrincipal me, @PathVariable String username) {
        return socialService.followGym(me.getUsername(), username);
    }

    @Operation(summary = "Unfollow a gym")
    @DeleteMapping("/following/gyms/{username}")
    public FollowStatusDTO unfollowGym(@AuthenticationPrincipal UserPrincipal me, @PathVariable String username) {
        return socialService.unfollowGym(me.getUsername(), username);
    }

    // Liste follow del trainer autenticato
    @Operation(summary = "Get the trainers the authenticated user follows")
    @GetMapping("/following/trainers")
    public List<TrainerNode> getFollowingTrainers(@AuthenticationPrincipal UserPrincipal me) {
        return socialService.getFollowingTrainers(me.getUsername());
    }

    @Operation(summary = "Get the gyms the authenticated user follows")
    @GetMapping("/following/gyms")
    public List<GymNode> getFollowingGyms(@AuthenticationPrincipal UserPrincipal me) {
        return socialService.getFollowingGyms(me.getUsername());
    }

    @Operation(summary = "Get recommended trainers to follow")
    @GetMapping("/recommendations/trainers")
    public List<RecommendedTrainerDTO> getRecommendedTrainers(@AuthenticationPrincipal UserPrincipal me) {
        return socialService.getRecommendedTrainers(me.getUsername());
    }

    @Operation(summary = "Get recommended tournaments")
    @GetMapping("/recommendations/tournaments")
    public List<RecommendedTournamentDTO> getRecommendedTournaments(@AuthenticationPrincipal UserPrincipal me) {
        return socialService.getRecommendedTournaments(me.getUsername());
    }

    @Operation(summary = "Get tournaments attended by followed pro trainers")
    @GetMapping("/recommendations/pro-tournaments")
    public List<ProTournamentDTO> getProTournaments(@AuthenticationPrincipal UserPrincipal me) {
        return socialService.getProTournaments(me.getUsername());
    }

    @Operation(summary = "Get the authenticated trainer's personal community")
    @GetMapping("/community")
    public List<CommunityDTO> getCommunity(@AuthenticationPrincipal UserPrincipal me) {
        return socialService.getCommunity(me.getUsername());
    }

    @Operation(summary = "Get the authenticated gym's most loyal trainer pairs")
    @GetMapping("/loyal-trainers")
    public List<LoyalTrainersDTO> getLoyalTrainers(@AuthenticationPrincipal UserPrincipal me) {
        return socialService.getLoyalTrainers(me.getUsername());
    }
}
