package it.unipi.poketcgnet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.poketcgnet.dto.DeletedAccountDTO;
import it.unipi.poketcgnet.dto.GymDTO;
import it.unipi.poketcgnet.dto.GymRatingDTO;
import it.unipi.poketcgnet.dto.GymRegistrationRequest;
import it.unipi.poketcgnet.dto.PasswordChangeRequest;
import it.unipi.poketcgnet.dto.ReviewRequest;
import it.unipi.poketcgnet.dto.SliceDTO;
import it.unipi.poketcgnet.service.GymService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Gyms", description = "Endpoints for managing gym profiles, registration and account operations")
@RestController
@RequestMapping("/api/gyms")
public class GymController {

    private final GymService gymService;

    public GymController(GymService gymService) {
        this.gymService = gymService;
    }

    @Operation(summary = "Get a paginated list of gyms")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/page/{pageNumber}")
    public SliceDTO<GymDTO> getGymsPage(@PathVariable int pageNumber) {
        return gymService.getGymsPage(pageNumber);
    }

    @Operation(summary = "Get a gym's profile by username")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{username}")
    public GymDTO getGymByUsername(@PathVariable String username) {
        return gymService.getGymByUsername(username);
    }

    // Lista "gym vicine a te": gym della prefettura ordinate per voto medio delle
    // recensioni.
    // Pubblica come le altre GET di catalogo
    @Operation(summary = "Get gyms in a prefecture ranked by average review rating")
    @GetMapping("/prefecture/{prefecture}/ranked")
    public List<GymRatingDTO> getGymsByPrefectureRankedByRating(@PathVariable String prefecture) {
        return gymService.getGymsByPrefectureRankedByRating(prefecture);
    }

    @Operation(summary = "Search gyms by username or shop name")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/search")
    public List<GymDTO> searchGyms(@RequestParam String q) {
        return gymService.searchGyms(q);
    }

    @Operation(summary = "Change the authenticated gym's password")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody PasswordChangeRequest request,
            Authentication authentication) {
        gymService.changePassword(authentication.getName(), request);
    }

    @Operation(summary = "Register a new gym account")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public GymDTO registerGym(@Valid @RequestBody GymRegistrationRequest request) {
        return gymService.registerGym(request);
    }

    @Operation(summary = "Leave or update a review for a gym")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{gymUsername}/reviews")
    @PreAuthorize("hasRole('TRAINER')")
    public GymDTO reviewGym(@PathVariable String gymUsername,
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {
        return gymService.reviewGym(gymUsername, authentication.getName(), request);
    }

    // Elimina una recensione: il TRAINER solo la propria (reviewerUsername deve
    // essere
    // il suo), l'ADMIN qualsiasi (moderazione)
    @Operation(summary = "Delete a review left on a gym")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{gymUsername}/reviews/{reviewerUsername}")
    public GymDTO deleteReview(@PathVariable String gymUsername,
            @PathVariable String reviewerUsername,
            Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return gymService.deleteReview(gymUsername, reviewerUsername, authentication.getName(), isAdmin);
    }

    // Il gym elimina il proprio account (cascata sui tornei pendenti)
    @Operation(summary = "Delete the authenticated gym's account")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/me")
    public DeletedAccountDTO deleteMyAccount(Authentication authentication) {
        return gymService.deleteAccount(authentication.getName());
    }

    // Moderazione ADMIN: elimina l'account di un gym altrui (stessa cascata).
    @Operation(summary = "Delete another gym's account (admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{username}")
    public DeletedAccountDTO deleteAccount(@PathVariable String username) {
        return gymService.deleteAccount(username);
    }
}
