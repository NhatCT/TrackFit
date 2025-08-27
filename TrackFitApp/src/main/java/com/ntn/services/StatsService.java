package com.ntn.services;

import com.ntn.dto.StatsSummaryDTO;
import java.util.Date;

public interface StatsService {
    // Dùng cho ADMIN controller: toàn hệ thống
    StatsSummaryDTO summarySystem(Date from, Date to);

    // Dùng cho API: theo user (userId)
    StatsSummaryDTO summaryUser(String userId, Date from, Date to);
}
