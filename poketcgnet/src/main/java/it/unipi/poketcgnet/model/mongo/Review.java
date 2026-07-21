package it.unipi.poketcgnet.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    private String userId;
    private String username;
    private String textReview;
    private Integer score;
    private LocalDate dateReview;
}
