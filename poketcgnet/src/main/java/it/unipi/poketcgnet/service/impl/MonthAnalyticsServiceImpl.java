package it.unipi.poketcgnet.service.impl;

import it.unipi.poketcgnet.dto.MonthAnalyticsDTO;
import it.unipi.poketcgnet.dto.MonthlySubscribesDTO;
import it.unipi.poketcgnet.dto.MonthlyTournamentsDTO;
import it.unipi.poketcgnet.model.mongo.MonthAnalytics;
import it.unipi.poketcgnet.repository.mongo.MonthAnalyticsRepository;
import it.unipi.poketcgnet.repository.mongo.TournamentRepository;
import it.unipi.poketcgnet.repository.mongo.TrainerRepository;
import it.unipi.poketcgnet.service.MonthAnalyticsService;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MonthAnalyticsServiceImpl implements MonthAnalyticsService {

    private final MonthAnalyticsRepository monthAnalyticsRepository;
    private final TrainerRepository trainerRepository;
    private final TournamentRepository tournamentRepository;

    // Quanti mesi (incluso quello corrente) restano nella finestra di refresh
    // incrementale: 13 per poter calcolare il delta anche sul dodicesimo mese
    // restituito in lettura
    private static final int REFRESH_WINDOW_MONTHS = 13;

    public MonthAnalyticsServiceImpl(MonthAnalyticsRepository monthAnalyticsRepository,
            TrainerRepository trainerRepository,
            TournamentRepository tournamentRepository) {
        this.monthAnalyticsRepository = monthAnalyticsRepository;
        this.trainerRepository = trainerRepository;
        this.tournamentRepository = tournamentRepository;
    }

    @Override
    public List<MonthAnalyticsDTO> getRecentMonths() {
        refreshRecentMonths();
        return monthAnalyticsRepository.findTop13ByOrderByYearDescMonthDesc().stream()
                .map(this::toDTO)
                .toList();
    }

    private void refreshRecentMonths() {
        YearMonth from = YearMonth.now().minusMonths(REFRESH_WINDOW_MONTHS - 1L);
        String fromDate = from.atDay(1).toString();

        Map<String, Integer> subscribesByMonth = new HashMap<>();
        for (MonthlySubscribesDTO s : trainerRepository.aggregateSubscribesSince(fromDate)) {
            subscribesByMonth.put(s.getMonth(), s.getNewSubscribes());
        }
        Map<String, Integer> tournamentsByMonth = new HashMap<>();
        for (MonthlyTournamentsDTO t : tournamentRepository.aggregateTournamentsSince(fromDate)) {
            tournamentsByMonth.put(t.getMonth(), t.getNewTournaments());
        }

        List<MonthAnalytics> toSave = new ArrayList<>();
        for (YearMonth ym = from; !ym.isAfter(YearMonth.now()); ym = ym.plusMonths(1)) {
            String id = ym.toString();
            MonthAnalytics m = monthAnalyticsRepository.findById(id).orElseGet(MonthAnalytics::new);
            m.setId(id);
            m.setYear(ym.getYear());
            m.setMonth(ym.getMonthValue());
            m.setNewSubscribes(subscribesByMonth.getOrDefault(id, 0));
            m.setNewTournaments(tournamentsByMonth.getOrDefault(id, 0));
            toSave.add(m);
        }
        monthAnalyticsRepository.saveAll(toSave);
    }

    private MonthAnalyticsDTO toDTO(MonthAnalytics monthAnalytics) {
        return new MonthAnalyticsDTO(
                monthAnalytics.getId(),
                monthAnalytics.getYear(),
                monthAnalytics.getMonth(),
                monthAnalytics.getNewSubscribes(),
                monthAnalytics.getNewTournaments());
    }
}
