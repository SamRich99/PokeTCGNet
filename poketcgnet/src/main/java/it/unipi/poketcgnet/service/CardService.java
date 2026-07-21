package it.unipi.poketcgnet.service;

import it.unipi.poketcgnet.dto.CardDTO;
import it.unipi.poketcgnet.dto.CardRequest;
import it.unipi.poketcgnet.dto.SliceDTO;

import java.util.List;

public interface CardService {

    SliceDTO<CardDTO> getCardsPage(int pageNumber);

    CardDTO getCardById(String id);

    List<CardDTO> getCardsByPokemonType(String pokemonType);

    List<CardDTO> getCardsByCardCategory(String cardCategory);

    List<CardDTO> searchCardsByName(String name);

    CardDTO createCard(CardRequest request);

    CardDTO updateCard(String id, CardRequest request);

    void deleteCard(String id);
}
