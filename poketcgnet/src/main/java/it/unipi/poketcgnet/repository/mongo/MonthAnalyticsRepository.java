package it.unipi.poketcgnet.repository.mongo;

import it.unipi.poketcgnet.model.mongo.MonthAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MonthAnalyticsRepository extends MongoRepository<MonthAnalytics, String> {

    // Storico recente da restituire in lettura
    List<MonthAnalytics> findTop13ByOrderByYearDescMonthDesc();
}
