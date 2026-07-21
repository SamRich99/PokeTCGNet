package it.unipi.poketcgnet.service;

import it.unipi.poketcgnet.dto.ArchetypeTrendDTO;
import it.unipi.poketcgnet.dto.DeckDTO;
import it.unipi.poketcgnet.dto.DeckRequest;
import it.unipi.poketcgnet.dto.SliceDTO;

import java.util.List;

public interface DeckService {

    SliceDTO<DeckDTO> getDecksPage(int pageNumber);

    DeckDTO getDeckById(String id);

    List<DeckDTO> getDecksByOwnerUsername(String ownerUsername);

    List<DeckDTO> getDecksByArchetypeId(String archetypeId);

    DeckDTO createDeck(String ownerUsername, DeckRequest request);

    DeckDTO updateDeck(String deckId, String ownerUsername, DeckRequest request);

    void deleteDeck(String deckId, String requesterUsername, boolean isAdmin);

    List<ArchetypeTrendDTO> getArchetypeTrends(int days);
}
