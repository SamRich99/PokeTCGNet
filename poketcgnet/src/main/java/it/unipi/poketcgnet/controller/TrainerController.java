package it.unipi.poketcgnet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.poketcgnet.dto.DeletedAccountDTO;
import it.unipi.poketcgnet.dto.MonthlySubscribesDTO;
import it.unipi.poketcgnet.dto.PasswordChangeRequest;
import it.unipi.poketcgnet.dto.SliceDTO;
import it.unipi.poketcgnet.dto.TrainerDTO;
import it.unipi.poketcgnet.dto.TrainerKpiDTO;
import it.unipi.poketcgnet.dto.TrainerRegistrationRequest;
import it.unipi.poketcgnet.service.TrainerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Trainers", description = "Endpoints for managing trainer profiles, registration and account operations")
@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @Operation(summary = "Get a paginated list of trainers")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/page/{pageNumber}")
    public SliceDTO<TrainerDTO> getTrainersPage(@PathVariable int pageNumber) {
        return trainerService.getTrainersPage(pageNumber);
    }

    @Operation(summary = "Get a trainer's profile by username")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{username}")
    public TrainerDTO getTrainerByUsername(@PathVariable String username) {
        return trainerService.getTrainerByUsername(username);
    }

    // analytics admin: iscrizioni per mese con delta
    @Operation(summary = "Get monthly trainer registration statistics (admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/analytics/subscriptions")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MonthlySubscribesDTO> getMonthlySubscribes() {
        return trainerService.getMonthlySubscribes();
    }

    // KPI del trainer per archetipo
    @Operation(summary = "Get a trainer's KPIs grouped by archetype")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{username}/kpi")
    public List<TrainerKpiDTO> getTrainerKpi(@PathVariable String username) {
        return trainerService.getTrainerKpi(username);
    }

    @Operation(summary = "Search trainers by username or name")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/search")
    public List<TrainerDTO> searchTrainers(@RequestParam String q) {
        return trainerService.searchTrainers(q);
    }

    @Operation(summary = "Change the authenticated trainer's password")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody PasswordChangeRequest request,
            Authentication authentication) {
        trainerService.changePassword(authentication.getName(), request);
    }

    @Operation(summary = "Register a new trainer account")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TrainerDTO registerTrainer(@Valid @RequestBody TrainerRegistrationRequest request) {
        return trainerService.registerTrainer(request);
    }

    // Self-service: il trainer elimina il proprio account (cascata su tornei/deck)
    @Operation(summary = "Delete the authenticated trainer's account")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/me")
    public DeletedAccountDTO deleteMyAccount(Authentication authentication) {
        return trainerService.deleteAccount(authentication.getName());
    }

    // Moderazione ADMIN: elimina l'account di un trainer altrui (stessa cascata).
    @Operation(summary = "Delete another trainer's account (admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{username}")
    public DeletedAccountDTO deleteAccount(@PathVariable String username) {
        return trainerService.deleteAccount(username);
    }

    // Fine stagione: azzera currentSeasonPoints/isPro per tutti i trainer.
    @Operation(summary = "Reset every trainer's current-season points and pro status (admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/end-season")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void endSeason() {
        trainerService.endSeason();
    }

    @Operation(summary = "Recompute which trainers qualify as Pro for the current season (admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/recompute-pro-status")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recomputeProStatus() {
        trainerService.recomputeProStatus();
    }
}
