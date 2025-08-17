package com.ntn.services;

import com.ntn.dto.StatsSummaryDTO;
import java.util.Date;

public interface StatsService {
    StatsSummaryDTO summary(String username, Date from, Date to);
}
