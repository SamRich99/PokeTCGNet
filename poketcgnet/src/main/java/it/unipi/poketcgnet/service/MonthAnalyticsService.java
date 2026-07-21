package it.unipi.poketcgnet.service;

import it.unipi.poketcgnet.dto.MonthAnalyticsDTO;

import java.util.List;

public interface MonthAnalyticsService {

    // Refresh incrementale + lettura
    List<MonthAnalyticsDTO> getRecentMonths();
}
