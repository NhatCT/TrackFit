package com.ntn.services;

import com.ntn.dto.StatsSummaryDTO;
import java.util.Date;

public interface StatsService {

    StatsSummaryDTO summarySystem(Date from, Date to);

    StatsSummaryDTO summaryUser(String userId, Date from, Date to);
}
