// src/utils/useTodaySummary.js
// Hook gom dữ liệu cho dashboard "Tóm tắt": chuỗi ngày, số buổi/tuần, phút hôm
// nay và mục tiêu — tất cả suy ra từ API sẵn có (histories + goals), không cần
// backend mới. Tự cập nhật khi có sự kiện "trackfit-notification".
import { useEffect, useState } from "react";
import { authApis, endpoints } from "../configs/Apis";

const DOW_SHORT = ["CN", "T2", "T3", "T4", "T5", "T6", "T7"];

/** Danh hiệu + mốc kế tiếp theo số ngày chuỗi (đồng bộ với StreakWidget) */
export function badgeFor(streak) {
  if (streak >= 90) return { emoji: "👑", label: "Huyền thoại", color: "danger", next: null };
  if (streak >= 30) return { emoji: "🏆", label: "Chiến thần", color: "warning", next: 90 };
  if (streak >= 7) return { emoji: "💪", label: "Chăm chỉ", color: "success", next: 30 };
  if (streak >= 1) return { emoji: "🔥", label: "Khởi động", color: "primary", next: 7 };
  return { emoji: "💤", label: "Nghỉ ngơi", color: "secondary", next: 1 };
}

export function useTodaySummary(enabled = true) {
  const [data, setData] = useState({
    loading: true,
    streak: 0,
    last7: [],
    weekSessions: 0,
    weekTarget: 7,
    todaySessions: 0,
    todayMinutes: 0,
    minutesTarget: 30,
  });

  const load = async () => {
    try {
      const [hRes, gRes] = await Promise.all([
        authApis().get(endpoints.histories, { params: { status: "COMPLETED", pageSize: 0 } }),
        authApis().get(endpoints.goals).catch(() => ({ data: null })),
      ]);

      const histories = (hRes.data?.items || []).filter((h) => h.completedAt);

      // Mục tiêu phút/buổi lấy từ goal mới nhất (nếu có)
      const goals = Array.isArray(gRes.data) ? gRes.data : (gRes.data?.items || []);
      let minutesTarget = 30;
      if (goals && goals.length) {
        const latest = [...goals].sort(
          (a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0)
        )[0];
        if (latest?.workoutDuration) minutesTarget = Number(latest.workoutDuration) || 30;
      }

      const completedDates = new Set(
        histories.map((h) => new Date(h.completedAt).toDateString())
      );

      // Tính chuỗi ngày liên tiếp (tính từ hôm nay, hoặc hôm qua nếu hôm nay chưa tập)
      let streak = 0;
      const today = new Date();
      const hasToday = completedDates.has(today.toDateString());
      const yest = new Date();
      yest.setDate(yest.getDate() - 1);
      const start = hasToday ? new Date() : (completedDates.has(yest.toDateString()) ? yest : null);
      if (start) {
        const curr = new Date(start);
        streak = 1;
        // eslint-disable-next-line no-constant-condition
        while (true) {
          curr.setDate(curr.getDate() - 1);
          if (completedDates.has(curr.toDateString())) streak++;
          else break;
        }
      }

      // 7 ngày gần nhất (kết thúc hôm nay)
      const last7 = [];
      for (let i = 6; i >= 0; i--) {
        const d = new Date();
        d.setDate(d.getDate() - i);
        last7.push({
          dayName: DOW_SHORT[d.getDay()],
          completed: completedDates.has(d.toDateString()),
          isToday: i === 0,
        });
      }

      // Phút & số buổi hôm nay
      const todayStr = today.toDateString();
      const todayHist = histories.filter((h) => new Date(h.completedAt).toDateString() === todayStr);
      const todayMinutes = todayHist.reduce((s, h) => s + (Number(h.duration) || 0), 0);
      const todaySessions = todayHist.length;
      const weekSessions = last7.filter((d) => d.completed).length;

      setData({
        loading: false,
        streak,
        last7,
        weekSessions,
        weekTarget: 7,
        todaySessions,
        todayMinutes,
        minutesTarget,
      });
    } catch (e) {
      console.error("useTodaySummary error:", e);
      setData((d) => ({ ...d, loading: false }));
    }
  };

  useEffect(() => {
    if (!enabled) {
      setData((d) => ({ ...d, loading: false }));
      return;
    }
    load();
    const onUpd = () => load();
    window.addEventListener("trackfit-notification", onUpd);
    return () => window.removeEventListener("trackfit-notification", onUpd);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [enabled]);

  return data;
}
