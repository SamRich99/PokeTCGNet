package it.unipi.poketcgnet.service.impl;

import it.unipi.poketcgnet.dto.ArchetypeDTO;
import it.unipi.poketcgnet.dto.ArchetypeTrendDTO;
import it.unipi.poketcgnet.dto.DeckCardDTO;
import it.unipi.poketcgnet.dto.DeckDTO;
import it.unipi.poketcgnet.dto.DeckRequest;
import it.unipi.poketcgnet.dto.SliceDTO;
import it.unipi.poketcgnet.model.mongo.Card;
import it.unipi.poketcgnet.model.mongo.Deck;
import it.unipi.poketcgnet.model.mongo.DeckCard;
import it.unipi.poketcgnet.model.mongo.Trainer;
import it.unipi.poketcgnet.repository.mongo.CardRepository;
import it.unipi.poketcgnet.repository.mongo.DeckRepository;
import it.unipi.poketcgnet.repository.mongo.TournamentRepository;
import it.unipi.poketcgnet.repository.mongo.TrainerRepository;
import it.unipi.poketcgnet.service.ArchetypeService;
import it.unipi.poketcgnet.service.DeckService;
import it.unipi.poketcgnet.service.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DeckServiceImpl implements DeckService {

    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final ArchetypeService archetypeService;
    private final TrainerRepository trainerRepository;
    private final TournamentRepository tournamentRepository;

    @Value("${app.pagination.decks:20}")
    private int pageSize;

    public DeckServiceImpl(DeckRepository deckRepository, CardRepository cardRepository,
            ArchetypeService archetypeService, TrainerRepository trainerRepository,
            TournamentRepository tournamentRepository) {
        this.deckRepository = deckRepository;
        this.cardRepository = cardRepository;
        this.archetypeService = archetypeService;
        this.trainerRepository = trainerRepository;
        this.tournamentRepository = tournamentRepository;
    }

    @Override
    public SliceDTO<DeckDTO> getDecksPage(int pageNumber) {
        if (pageNumber <= 0) {
            throw new IllegalArgumentException("Numero di pagina non valido: " + pageNumber);
        }
        // ordinamento per _id
        Slice<Deck> slice = deckRepository.findAllBy(
                PageRequest.of(pageNumber - 1, pageSize, Sort.by("_id")));
        return new SliceDTO<>(slice.getContent().stream().map(this::toDTO).toList(),
                slice.hasNext(), slice.hasPrevious());
    }

    @Override
    public DeckDTO getDeckById(String id) {
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deck non trovato: " + id));
        return toDTO(deck);
    }

    @Override
    public List<DeckDTO> getDecksByOwnerUsername(String ownerUsername) {
        return trainerRepository.findByUsername(ownerUsername)
                .map(u -> deckRepository.findByOwnerId(u.getId()))
                .orElseGet(List::of)
                .stream().map(this::toDTO).toList();
    }

    @Override
    public List<DeckDTO> getDecksByArchetypeId(String archetypeId) {
        return deckRepository.findByArchetypeIdOrderByCompetitivePointDesc(archetypeId).stream().map(this::toDTO)
                .toList();
    }

    @Override
    public DeckDTO createDeck(String ownerUsername, DeckRequest request) {
        Deck deck = buildDeck(ownerUsername, request);
        return toDTO(deckRepository.save(deck));
    }

    @Override
    public DeckDTO updateDeck(String deckId, String ownerUsername, DeckRequest request) {
        Deck current = deckRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Deck non trovato: " + deckId));
        if (!resolveUserId(ownerUsername).equals(current.getOwnerId())) {
            throw new AccessDeniedException("Il deck appartiene a un altro trainer");
        }
        if (Boolean.TRUE.equals(current.getDeleted())) {
            throw new ResourceNotFoundException("Deck non trovato: " + deckId);
        }

        // Copy-on-write: la versione giocata resta immutabile
        current.setDeleted(true);
        deckRepository.save(current);

        Deck newVersion = buildDeck(ownerUsername, request);
        return toDTO(deckRepository.save(newVersion));
    }

    @Override
    public void deleteDeck(String deckId, String requesterUsername, boolean isAdmin) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Deck non trovato: " + deckId));

        if (isAdmin) {
            // hard delete: il deck sparisce anche dal drill-down dei tornei
            // (il client mostrerà "deck non più disponibile" sul 404)
            deckRepository.deleteById(deckId);
            return;
        }

        if (!resolveUserId(requesterUsername).equals(deck.getOwnerId())) {
            throw new AccessDeniedException("Il deck appartiene a un altro trainer");
        }
        // soft delete: sparisce dalle liste ma resta visibile dai tornei giocati
        deck.setDeleted(true);
        deckRepository.save(deck);
    }

    @Override
    public List<ArchetypeTrendDTO> getArchetypeTrends(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Numero di giorni non valido: " + days);
        }
        String cutoff = LocalDate.now().minusDays(days).toString();
        return tournamentRepository.aggregateArchetypeTrends(cutoff);
    }

    private Deck buildDeck(String ownerUsername, DeckRequest request) {
        List<String> requestedIds = request.getCards().stream()
                .map(DeckRequest.DeckCardRequest::getCardId)
                .toList();
        Map<String, Card> cardsById = cardRepository.findAllById(requestedIds).stream()
                .collect(Collectors.toMap(Card::getId, c -> c));

        List<DeckCard> cards = new ArrayList<>();
        Map<String, Integer> quantityPerCard = new HashMap<>();
        int total = 0;

        for (DeckRequest.DeckCardRequest rc : request.getCards()) {
            Card card = cardsById.get(rc.getCardId());
            if (card == null) {
                throw new IllegalArgumentException("Carta inesistente: " + rc.getCardId());
            }

            int already = quantityPerCard.merge(card.getId(), rc.getQuantity(), Integer::sum);
            if (already > 4 && !"Energy".equals(card.getCardCategory())) {
                throw new IllegalArgumentException(
                        "Massimo 4 copie per carta (escluse le energie): " + card.getName());
            }
            total += rc.getQuantity();
            cards.add(new DeckCard(card.getId(), card.getName(), rc.getQuantity()));
        }
        if (total != 60) {
            throw new IllegalArgumentException("Un deck deve contenere esattamente 60 carte, trovate: " + total);
        }

        String archetypeId;
        String archetypeName;
        if (request.getArchetypeId() != null) {
            // Il client ha già chiamato /api/archetypes/match e rimanda indietro il
            // candidato scelto: ci fidiamo di id+nome così come sono, niente query
            // di matching da rifare/validare per queste carte.
            if (request.getArchetypeName() == null || request.getArchetypeName().isBlank()) {
                throw new IllegalArgumentException("archetypeName obbligatorio insieme ad archetypeId");
            }
            archetypeId = request.getArchetypeId();
            archetypeName = request.getArchetypeName();
        } else {
            List<String> cardIds = cards.stream().map(DeckCard::getCardId).toList();
            List<ArchetypeDTO> matches = archetypeService.findMatchingArchetypes(cardIds);
            ArchetypeDTO best = matches.isEmpty() ? null : matches.get(0);
            archetypeId = best != null ? best.getId() : null;
            archetypeName = best != null ? best.getName() : null;
        }

        Deck deck = new Deck();
        deck.setName(request.getName());
        deck.setOwnerId(resolveUserId(ownerUsername));
        deck.setOwnerUsername(ownerUsername);
        deck.setArchetypeId(archetypeId);
        deck.setArchetypeName(archetypeName);
        deck.setCreatedAt(LocalDate.now());
        deck.setCompetitivePoint(0);
        deck.setCards(cards);
        deck.setDeleted(false);
        return deck;
    }

    // username (dal JWT) -> id interno dell'utente attuale: è l'id, non lo
    // username, a stabilire l'ownership dei deck
    private String resolveUserId(String username) {
        return trainerRepository.findByUsername(username)
                .map(Trainer::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato: " + username));
    }

    private DeckDTO toDTO(Deck deck) {
        List<DeckCardDTO> cards = deck.getCards().stream().map(this::toDTO).toList();

        return new DeckDTO(
                deck.getId(),
                deck.getName(),
                deck.getOwnerUsername(),
                deck.getArchetypeId(),
                deck.getArchetypeName(),
                deck.getCreatedAt(),
                deck.getCompetitivePoint(),
                cards);
    }

    private DeckCardDTO toDTO(DeckCard deckCard) {
        return new DeckCardDTO(deckCard.getCardId(), deckCard.getName(), deckCard.getQuantity());
    }
}
