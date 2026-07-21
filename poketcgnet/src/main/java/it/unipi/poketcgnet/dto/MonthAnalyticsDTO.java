package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthAnalyticsDTO {

    private String id;
    private Integer year;
    private Integer month;
    private Integer newSubscribes;
    private Integer newTournaments;
}
