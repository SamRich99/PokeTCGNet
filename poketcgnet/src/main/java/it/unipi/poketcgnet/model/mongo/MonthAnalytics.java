package it.unipi.poketcgnet.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "month_analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthAnalytics {

    @Id
    private String id;
    private Integer year;
    private Integer month;
    private Integer newSubscribes;
    private Integer newTournaments;
}
