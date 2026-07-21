package it.unipi.poketcgnet.service.impl;

import it.unipi.poketcgnet.dto.ArchetypeDTO;
import it.unipi.poketcgnet.dto.ArchetypeRequest;
import it.unipi.poketcgnet.dto.CoreCardDTO;
import it.unipi.poketcgnet.model.mongo.Archetype;
import it.unipi.poketcgnet.model.mongo.CoreCard;
import it.unipi.poketcgnet.repository.mongo.ArchetypeRepository;
import it.unipi.poketcgnet.repository.mongo.CardRepository;
import it.unipi.poketcgnet.service.ArchetypeService;
import it.unipi.poketcgnet.service.exception.ResourceAlreadyExistsException;
import it.unipi.poketcgnet.service.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class ArchetypeServiceImpl implements ArchetypeService {

    private final ArchetypeRepository archetypeRepository;
    private final CardRepository cardRepository;

    public ArchetypeServiceImpl(ArchetypeRepository archetypeRepository, CardRepository cardRepository) {
        this.archetypeRepository = archetypeRepository;
        this.cardRepository = cardRepository;
    }

    @Override
    public List<ArchetypeDTO> getAllArchetypes() {
        return archetypeRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    public ArchetypeDTO getArchetypeById(String id) {
        Archetype archetype = archetypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Archetipo non trovato: " + id));
        return toDTO(archetype);
    }

    @Override
    public List<ArchetypeDTO> searchArchetypesByName(String name) {
        return archetypeRepository.findByName(name).stream().map(this::toDTO).toList();
    }

    // Ranking dei candidati: non il numero grezzo di core cards in comune, ma la
    // frazione (0..1) del "peso" dell'archetipo coperta dal deck
    @Override
    public List<ArchetypeDTO> findMatchingArchetypes(List<String> cardIds) {
        Set<String> deckCardIds = Set.copyOf(cardIds);

        return archetypeRepository.findByCoreCards_CardIdIn(cardIds).stream()
                .sorted(Comparator.comparingDouble((Archetype archetype) -> matchScore(archetype, deckCardIds))
                        .reversed())
                .limit(5)
                .map(this::toDTO)
                .toList();
    }

    private double matchScore(Archetype archetype, Set<String> deckCardIds) {
        double totalWeight = archetype.getCoreCards().stream()
                .mapToDouble(c -> c.getPresence() != null ? c.getPresence() : 0.0)
                .sum();
        if (totalWeight <= 0) {
            return 0.0;
        }
        double matchedWeight = archetype.getCoreCards().stream()
                .filter(c -> deckCardIds.contains(c.getCardId()))
                .mapToDouble(c -> c.getPresence() != null ? c.getPresence() : 0.0)
                .sum();
        return matchedWeight / totalWeight;
    }

    @Override
    public ArchetypeDTO createArchetype(ArchetypeRequest request) {
        // Chiave naturale (come i cataloghi Card), non ObjectId: slug del nome
        String id = slugify(request.getName());
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Nome archetipo non valido: " + request.getName());
        }
        if (archetypeRepository.existsById(id)) {
            throw new ResourceAlreadyExistsException("Archetipo gia' presente: " + id);
        }
        Archetype archetype = new Archetype();
        archetype.setId(id);
        archetype.setName(request.getName());
        archetype.setCoreCards(toCoreCards(request));
        return toDTO(archetypeRepository.save(archetype));
    }

    // Slug _id dal nome: minuscolo, sequenze non alfanumeriche -> '-', niente '-' ai bordi
    private String slugify(String name) {
        return name == null ? ""
                : name.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    @Override
    public ArchetypeDTO updateArchetype(String id, ArchetypeRequest request) {
        Archetype archetype = archetypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Archetipo non trovato: " + id));
        archetype.setName(request.getName());
        archetype.setCoreCards(toCoreCards(request));
        return toDTO(archetypeRepository.save(archetype));
    }

    @Override
    public void deleteArchetype(String id) {
        if (!archetypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Archetipo non trovato: " + id);
        }
        archetypeRepository.deleteById(id);
    }

    private List<CoreCard> toCoreCards(ArchetypeRequest request) {
        if (request.getCoreCards() == null) {
            return new ArrayList<>();
        }
        List<CoreCard> coreCards = new ArrayList<>();
        for (ArchetypeRequest.CoreCardRequest rc : request.getCoreCards()) {
            if (!cardRepository.existsById(rc.getCardId())) {
                throw new IllegalArgumentException("Carta inesistente: " + rc.getCardId());
            }
            if (rc.getPresence() < 0 || rc.getPresence() > 1) {
                throw new IllegalArgumentException("presence deve essere tra 0 e 1: " + rc.getPresence());
            }
            coreCards.add(new CoreCard(rc.getCardId(), rc.getPresence()));
        }
        return coreCards;
    }

    private ArchetypeDTO toDTO(Archetype archetype) {
        List<CoreCardDTO> coreCards = archetype.getCoreCards().stream()
                .map(this::toDTO)
                .toList();

        return new ArchetypeDTO(
                archetype.getId(),
                archetype.getName(),
                coreCards);
    }

    private CoreCardDTO toDTO(CoreCard coreCard) {
        return new CoreCardDTO(coreCard.getCardId(), coreCard.getPresence());
    }
}
