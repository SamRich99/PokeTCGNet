package it.unipi.poketcgnet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.poketcgnet.dto.ArchetypeTrendDTO;
import it.unipi.poketcgnet.dto.DeckDTO;
import it.unipi.poketcgnet.dto.DeckRequest;
import it.unipi.poketcgnet.dto.SliceDTO;
import it.unipi.poketcgnet.service.DeckService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Decks", description = "Endpoints for managing decks")
@RestController
@RequestMapping("/api/decks")
public class DeckController {

    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @Operation(summary = "Get a paginated list of decks")
    @GetMapping("/page/{pageNumber}")
    public SliceDTO<DeckDTO> getDecksPage(@PathVariable int pageNumber) {
        return deckService.getDecksPage(pageNumber);
    }

    @Operation(summary = "Get a deck's details by id")
    @GetMapping("/{id}")
    public DeckDTO getDeckById(@PathVariable String id) {
        return deckService.getDeckById(id);
    }

    @Operation(summary = "Get all decks owned by a trainer")
    @GetMapping("/owner/{username}")
    public List<DeckDTO> getDecksByOwnerUsername(@PathVariable String username) {
        return deckService.getDecksByOwnerUsername(username);
    }

    @Operation(summary = "Get decks belonging to an archetype")
    @GetMapping("/archetype/{archetypeId}")
    public List<DeckDTO> getDecksByArchetypeId(@PathVariable String archetypeId) {
        return deckService.getDecksByArchetypeId(archetypeId);
    }

    // trend homepage: archetipi più usati nell'ultimo periodo
    @Operation(summary = "Get the most used archetypes in a recent time window")
    @GetMapping("/analytics/trends")
    public List<ArchetypeTrendDTO> getArchetypeTrends(@RequestParam(defaultValue = "90") int days) {
        return deckService.getArchetypeTrends(days);
    }

    @Operation(summary = "Create a new deck")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeckDTO createDeck(@Valid @RequestBody DeckRequest request, Authentication authentication) {
        return deckService.createDeck(authentication.getName(), request);
    }

    @Operation(summary = "Update a deck, creating a new copy-on-write version")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public DeckDTO updateDeck(@PathVariable String id, @Valid @RequestBody DeckRequest request,
            Authentication authentication) {
        return deckService.updateDeck(id, authentication.getName(), request);
    }

    // TRAINER: soft delete del proprio deck. ADMIN: eliminazione definitiva.
    @Operation(summary = "Delete a deck")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDeck(@PathVariable String id, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        deckService.deleteDeck(id, authentication.getName(), isAdmin);
    }
}
