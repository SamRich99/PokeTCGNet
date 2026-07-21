package it.unipi.poketcgnet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.poketcgnet.dto.CardDTO;
import it.unipi.poketcgnet.dto.CardRequest;
import it.unipi.poketcgnet.dto.SliceDTO;
import it.unipi.poketcgnet.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Cards", description = "Endpoints for browsing and managing the card catalog")
@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @Operation(summary = "Get a paginated list of cards")
    @GetMapping("/page/{pageNumber}")
    public SliceDTO<CardDTO> getCardsPage(@PathVariable int pageNumber) {
        return cardService.getCardsPage(pageNumber);
    }

    @Operation(summary = "Get a card's details by id")
    @GetMapping("/{id}")
    public CardDTO getCardById(@PathVariable String id) {
        return cardService.getCardById(id);
    }

    @Operation(summary = "Search cards by exact name")
    @GetMapping("/search")
    public List<CardDTO> searchByName(@RequestParam String name) {
        return cardService.searchCardsByName(name);
    }

    @Operation(summary = "Get cards by Pokémon type")
    @GetMapping("/type/{pokemonType}")
    public List<CardDTO> getByType(@PathVariable String pokemonType) {
        return cardService.getCardsByPokemonType(pokemonType);
    }

    @Operation(summary = "Get cards by card category")
    @GetMapping("/category/{cardCategory}")
    public List<CardDTO> getByCategory(@PathVariable String cardCategory) {
        return cardService.getCardsByCardCategory(cardCategory);
    }

    // Gestione catalogo (solo ADMIN)

    @Operation(summary = "Create a new card (admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardDTO createCard(@Valid @RequestBody CardRequest request) {
        return cardService.createCard(request);
    }

    @Operation(summary = "Update a card (admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public CardDTO updateCard(@PathVariable String id, @Valid @RequestBody CardRequest request) {
        return cardService.updateCard(id, request);
    }

    @Operation(summary = "Delete a card (admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable String id) {
        cardService.deleteCard(id);
    }
}
