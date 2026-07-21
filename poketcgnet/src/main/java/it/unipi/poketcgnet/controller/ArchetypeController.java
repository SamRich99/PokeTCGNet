package it.unipi.poketcgnet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.poketcgnet.dto.ArchetypeDTO;
import it.unipi.poketcgnet.dto.ArchetypeRequest;
import it.unipi.poketcgnet.service.ArchetypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Archetypes", description = "Endpoints for browsing and managing deck archetypes")
@RestController
@RequestMapping("/api/archetypes")
public class ArchetypeController {

    private final ArchetypeService archetypeService;

    public ArchetypeController(ArchetypeService archetypeService) {
        this.archetypeService = archetypeService;
    }

    @Operation(summary = "Get all archetypes")
    @GetMapping
    public List<ArchetypeDTO> getAllArchetypes() {
        return archetypeService.getAllArchetypes();
    }

    @Operation(summary = "Get an archetype's details by id")
    @GetMapping("/{id}")
    public ArchetypeDTO getArchetypeById(@PathVariable String id) {
        return archetypeService.getArchetypeById(id);
    }

    @Operation(summary = "Search archetypes by exact name")
    @GetMapping("/search")
    public List<ArchetypeDTO> searchByName(@RequestParam String name) {
        return archetypeService.searchArchetypesByName(name);
    }

    @Operation(summary = "Suggest matching archetypes for a set of cards")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/match")
    public List<ArchetypeDTO> findMatchingArchetypes(@RequestBody List<String> cardIds) {
        return archetypeService.findMatchingArchetypes(cardIds);
    }

    // Gestione archetipi (solo ADMIN)

    @Operation(summary = "Create a new archetype (admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArchetypeDTO createArchetype(@Valid @RequestBody ArchetypeRequest request) {
        return archetypeService.createArchetype(request);
    }

    @Operation(summary = "Update an archetype (admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ArchetypeDTO updateArchetype(@PathVariable String id,
            @Valid @RequestBody ArchetypeRequest request) {
        return archetypeService.updateArchetype(id, request);
    }

    @Operation(summary = "Delete an archetype (admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteArchetype(@PathVariable String id) {
        archetypeService.deleteArchetype(id);
    }
}
