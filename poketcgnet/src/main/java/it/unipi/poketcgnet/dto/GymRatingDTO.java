package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Risultato aggregation "gym per prefettura ordinate per voto": pensata per
// la lista "gym vicine a te" mostrata a un utente appena registrato. DTO snello e
// dedicato invece di riusare GymDTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GymRatingDTO {

    private String username;
    private String shopName;
    private String shopAddress;
    private String prefecture;
    private Double avgRating;
    private Integer reviewCount;
}
