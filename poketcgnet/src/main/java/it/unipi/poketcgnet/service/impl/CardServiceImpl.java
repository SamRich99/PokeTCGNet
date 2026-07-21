package it.unipi.poketcgnet.service.impl;

import it.unipi.poketcgnet.dto.CardDTO;
import it.unipi.poketcgnet.dto.CardRequest;
import it.unipi.poketcgnet.dto.SliceDTO;
import it.unipi.poketcgnet.model.mongo.Card;
import it.unipi.poketcgnet.repository.mongo.CardRepository;
import it.unipi.poketcgnet.service.CardService;
import it.unipi.poketcgnet.service.exception.ResourceAlreadyExistsException;
import it.unipi.poketcgnet.service.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;

    @Value("${app.pagination.cards:24}")
    private int pageSize;

    public CardServiceImpl(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Override
    public SliceDTO<CardDTO> getCardsPage(int pageNumber) {
        if (pageNumber <= 0) {
            throw new IllegalArgumentException("Numero di pagina non valido: " + pageNumber);
        }
        Slice<Card> slice = cardRepository.findAllBy(
                PageRequest.of(pageNumber - 1, pageSize, Sort.by("_id")));
        return new SliceDTO<>(slice.getContent().stream().map(this::toDTO).toList(),
                slice.hasNext(), slice.hasPrevious());
    }

    @Override
    public CardDTO getCardById(String id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card non trovata: " + id));
        return toDTO(card);
    }

    @Override
    public List<CardDTO> getCardsByPokemonType(String pokemonType) {
        return cardRepository.findByPokemonType(pokemonType).stream().map(this::toDTO).toList();
    }

    @Override
    public List<CardDTO> getCardsByCardCategory(String cardCategory) {
        return cardRepository.findByCardCategory(cardCategory).stream().map(this::toDTO).toList();
    }

    @Override
    public List<CardDTO> searchCardsByName(String name) {
        return cardRepository.findByName(name).stream().map(this::toDTO).toList();
    }

    @Override
    public CardDTO createCard(CardRequest request) {
        String id = request.getId() != null && !request.getId().isBlank()
                ? request.getId()
                : request.getSet() + "-" + request.getNumber();
        if (cardRepository.existsById(id)) {
            throw new ResourceAlreadyExistsException("Card già presente: " + id);
        }
        return toDTO(cardRepository.save(fromRequest(id, request)));
    }

    @Override
    public CardDTO updateCard(String id, CardRequest request) {
        if (!cardRepository.existsById(id)) {
            throw new ResourceNotFoundException("Card non trovata: " + id);
        }
        return toDTO(cardRepository.save(fromRequest(id, request)));
    }

    @Override
    public void deleteCard(String id) {
        if (!cardRepository.existsById(id)) {
            throw new ResourceNotFoundException("Card non trovata: " + id);
        }
        cardRepository.deleteById(id);
    }

    private Card fromRequest(String id, CardRequest request) {
        return new Card(
                id,
                request.getName(),
                request.getCardCategory(),
                request.getPokemonType(),
                request.getSet(),
                request.getSetName(),
                request.getNumber(),
                request.getRarity(),
                request.getImageUrl()
        );
    }

    private CardDTO toDTO(Card card) {
        return new CardDTO(
                card.getId(),
                card.getName(),
                card.getCardCategory(),
                card.getPokemonType(),
                card.getSet(),
                card.getSetName(),
                card.getNumber(),
                card.getRarity(),
                card.getImageUrl()
        );
    }
}
