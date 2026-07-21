package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeckDTO {

    private String id;
    private String name;
    private String ownerUsername;
    private String archetypeId;
    private String archetypeName;
    private LocalDate createdAt;
    private Integer competitivePoint;
    private List<DeckCardDTO> cards;
}
