// src/components/HealthTrendChart.js
// Biểu đồ xu hướng cân nặng/BMI dùng chung cho Home và HealthList (trước đây bị
// copy trùng ở cả hai). Màu lấy từ token qua chartTheme → tự đổi theo sáng/tối.
import { useMemo, useState } from "react";
import { Button } from "react-bootstrap";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Filler,
  Tooltip,
  Legend,
} from "chart.js";
import { Line } from "react-chartjs-2";
import { readTokens, withAlpha, axisTheme, useThemeVersion } from "../utils/chartTheme";

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Filler, Tooltip, Legend);

const fmtDate = (d) => {
  if (!d) return "";
  const dt = new Date(d);
  return `${String(dt.getDate()).padStart(2, "0")}/${String(dt.getMonth() + 1).padStart(2, "0")}`;
};
const bmiOf = (h, w) => {
  const ht = Number(h);
  const wt = Number(w);
  if (!ht || !wt) return null;
  return +(wt / Math.pow(ht / 100, 2)).toFixed(1);
};

export default function HealthTrendChart({ records = [], height = 230, defaultMode = "weight" }) {
  const [mode, setMode] = useState(defaultMode);
  const themeV = useThemeVersion();

  const sortedAsc = useMemo(
    () => [...records].sort((a, b) => new Date(a.createdAt || 0) - new Date(b.createdAt || 0)),
    [records]
  );
  const labels = useMemo(() => sortedAsc.map((h) => fmtDate(h.createdAt)), [sortedAsc]);
  const weightData = useMemo(() => sortedAsc.map((h) => Number(h.weight) || null), [sortedAsc]);
  const bmiData = useMemo(() => sortedAsc.map((h) => bmiOf(h.height, h.weight)), [sortedAsc]);

  const { data, options } = useMemo(() => {
    const t = readTokens();
    const color = mode === "bmi" ? t.brand : t.green;
    const chartData = {
      labels,
      datasets: [
        {
          label: mode === "bmi" ? "BMI" : "Cân nặng (kg)",
          data: mode === "bmi" ? bmiData : weightData,
          borderColor: color,
          backgroundColor: withAlpha(color, 0.16),
          fill: true,
          tension: 0.35,
          pointRadius: 4,
          pointHoverRadius: 6,
          pointBackgroundColor: color,
        },
      ],
    };
    const scales = axisTheme(t);
    if (mode === "bmi") {
      scales.y.suggestedMin = 14;
      scales.y.suggestedMax = 35;
    }
    const chartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            label: (ctx) => {
              if (mode === "bmi") {
                const v = ctx.raw;
                const lbl = v < 18.5 ? "Thiếu cân" : v < 23 ? "Bình thường" : v < 25 ? "Thừa cân" : "Béo phì";
                return `BMI: ${v} (${lbl})`;
              }
              return `${ctx.raw} kg`;
            },
          },
        },
      },
      scales,
    };
    return { data: chartData, options: chartOptions };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [labels, weightData, bmiData, mode, themeV]);

  if (sortedAsc.length === 0) {
    return (
      <div className="text-center py-5 text-muted">
        <div className="fs-1 mb-2">📊</div>
        <h6 className="fw-bold">Chưa có chỉ số sức khỏe</h6>
        <p className="text-muted small mb-3">Nhập thông số thể trạng để bắt đầu theo dõi.</p>
        <Button href="/health" variant="outline-primary" size="sm">+ Nhập chỉ số ngay</Button>
      </div>
    );
  }

  if (sortedAsc.length === 1) {
    return (
      <div
        className="text-center py-4 text-muted"
        style={{ border: "1px dashed var(--hair)", borderRadius: 12, margin: 10 }}
      >
        <div className="fs-2 mb-2">💡</div>
        <p className="m-0 px-3" style={{ fontSize: ".85rem" }}>
          Bạn mới có 1 bản ghi. Hãy nhập thêm cân nặng tại trang Sức khỏe để vẽ biểu đồ xu hướng!
        </p>
      </div>
    );
  }

  return (
    <>
      <div className="d-flex justify-content-end gap-1 mb-2">
        <Button
          size="sm"
          variant={mode === "weight" ? "primary" : "outline-secondary"}
          onClick={() => setMode("weight")}
          style={{ fontSize: ".75rem", padding: "4px 8px" }}
        >
          Cân nặng
        </Button>
        <Button
          size="sm"
          variant={mode === "bmi" ? "primary" : "outline-secondary"}
          onClick={() => setMode("bmi")}
          style={{ fontSize: ".75rem", padding: "4px 8px" }}
        >
          BMI
        </Button>
      </div>
      <div style={{ height, position: "relative" }}>
        <Line data={data} options={options} />
      </div>
      {mode === "bmi" && (
        <div
          className="d-flex gap-2 justify-content-center mt-3 flex-wrap text-muted"
          style={{ fontSize: ".65rem" }}
        >
          <span>● &lt;18.5 Thiếu</span>
          <span>● 18.5–22.9 Thường</span>
          <span>● 23–24.9 Thừa</span>
          <span>● ≥25 Béo phì</span>
        </div>
      )}
    </>
  );
}
