import { useEffect, useState, useMemo } from "react";
import { Badge } from "react-bootstrap";
import { authApis, endpoints } from "../configs/Apis";

const DOW_SHORT = ["CN", "T2", "T3", "T4", "T5", "T6", "T7"];

export default function StreakWidget() {
  const [streak, setStreak] = useState(0);
  const [recentCompletion, setRecentCompletion] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadStreak = async () => {
    try {
      const res = await authApis().get(endpoints.histories, {
        params: { status: "COMPLETED", pageSize: 0 },
      });
      const histories = res.data?.items || [];

      // Extract unique dates as toDateString
      const completedDates = new Set(
        histories
          .filter((h) => h.completedAt)
          .map((h) => new Date(h.completedAt).toDateString())
      );

      // Compute streak
      let currentStreak = 0;
      const today = new Date();
      const todayStr = today.toDateString();

      const yesterday = new Date();
      yesterday.setDate(yesterday.getDate() - 1);
      const yesterdayStr = yesterday.toDateString();

      const hasCompletedToday = completedDates.has(todayStr);
      const hasCompletedYesterday = completedDates.has(yesterdayStr);

      if (hasCompletedToday) {
        currentStreak = 1;
        const curr = new Date();
        while (true) {
          curr.setDate(curr.getDate() - 1);
          if (completedDates.has(curr.toDateString())) {
            currentStreak++;
          } else {
            break;
          }
        }
      } else if (hasCompletedYesterday) {
        currentStreak = 1;
        const curr = new Date();
        curr.setDate(curr.getDate() - 1);
        while (true) {
          curr.setDate(curr.getDate() - 1);
          if (completedDates.has(curr.toDateString())) {
            currentStreak++;
          } else {
            break;
          }
        }
      }

      setStreak(currentStreak);

      // Prepare last 7 days completions (ending today)
      const last7Days = [];
      for (let i = 6; i >= 0; i--) {
        const d = new Date();
        d.setDate(d.getDate() - i);
        last7Days.push({
          dateStr: d.toDateString(),
          dayName: DOW_SHORT[d.getDay()],
          completed: completedDates.has(d.toDateString()),
          isToday: i === 0,
        });
      }
      setRecentCompletion(last7Days);
    } catch (e) {
      console.error("Error loading streak data:", e);
      setError("Không tải được dữ liệu streak.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadStreak();

    // Listen for completion events to update streak in real-time
    const handleUpdate = () => {
      loadStreak();
    };
    window.addEventListener("trackfit-notification", handleUpdate);
    return () => {
      window.removeEventListener("trackfit-notification", handleUpdate);
    };
  }, []);

  const badgeInfo = useMemo(() => {
    if (streak >= 90) return { emoji: "👑", label: "Huyền thoại", color: "danger" };
    if (streak >= 30) return { emoji: "🏆", label: "Chiến thần", color: "warning" };
    if (streak >= 7) return { emoji: "💪", label: "Chăm chỉ", color: "success" };
    if (streak >= 1) return { emoji: "🔥", label: "Khởi động", color: "primary" };
    return { emoji: "💤", label: "Nghỉ ngơi", color: "secondary" };
  }, [streak]);

  if (loading) {
    return <span className="text-muted small">Đang tải...</span>;
  }

  if (error) {
    return <span className="text-muted small">{error}</span>;
  }

  return (
    <div className="d-flex flex-column gap-2 mt-2 align-items-start w-100">
      <div className="d-flex align-items-center gap-2">
        <span className="fs-3">{badgeInfo.emoji}</span>
        <div>
          <div className="fw-bold text-light" style={{ fontSize: "1.1rem" }}>
            {streak} ngày liên tiếp
          </div>
          <Badge bg={badgeInfo.color} style={{ fontSize: "0.75rem" }}>
            Danh hiệu: {badgeInfo.label}
          </Badge>
        </div>
      </div>

      {/* 7 day dots */}
      <div className="d-flex gap-2 justify-content-between w-100 mt-2 p-2 rounded" style={{ backgroundColor: "var(--surface-2)", border: "1px solid var(--border)" }}>
        {recentCompletion.map((day, idx) => (
          <div key={idx} className="d-flex flex-column align-items-center gap-1 flex-fill">
            <span style={{ fontSize: "0.7rem", color: day.isToday ? "var(--accent)" : "var(--muted)", fontWeight: day.isToday ? "bold" : "normal" }}>
              {day.dayName}
            </span>
            <div
              style={{
                width: "24px",
                height: "24px",
                borderRadius: "50%",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                fontSize: "0.75rem",
                fontWeight: "bold",
                backgroundColor: day.completed
                  ? "rgba(40, 167, 69, 0.25)"
                  : day.isToday
                  ? "rgba(255, 107, 53, 0.1)"
                  : "transparent",
                border: day.completed
                  ? "1px solid #28a745"
                  : day.isToday
                  ? "1px dashed var(--accent)"
                  : "1px solid var(--border)",
                color: day.completed ? "#28a745" : day.isToday ? "var(--accent)" : "var(--muted)",
              }}
              title={day.completed ? "Hoàn thành" : "Chưa hoàn thành"}
            >
              {day.completed ? "✓" : ""}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
