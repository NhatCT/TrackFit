// src/components/BloodPressureChart.js
// Biểu đồ xu hướng huyết áp (tâm thu/tâm trương) từ dữ liệu đã lưu sẵn trong
// health_data.bloodPressure ("120/80"). Màu lấy từ token → theo theme sáng/tối.
import { useMemo } from "react";
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
const parseBp = (s) => {
  const m = /^(\d{2,3})\/(\d{2,3})$/.exec((s || "").trim());
  if (!m) return null;
  return { sys: Number(m[1]), dia: Number(m[2]) };
};

export default function BloodPressureChart({ records = [], height = 240 }) {
  const themeV = useThemeVersion();

  const points = useMemo(() => {
    return [...records]
      .map((h) => ({ bp: parseBp(h.bloodPressure), createdAt: h.createdAt }))
      .filter((x) => x.bp)
      .sort((a, b) => new Date(a.createdAt || 0) - new Date(b.createdAt || 0));
  }, [records]);

  const { data, options } = useMemo(() => {
    const t = readTokens();
    const labels = points.map((p) => fmtDate(p.createdAt));
    const chartData = {
      labels,
      datasets: [
        {
          label: "Tâm thu",
          data: points.map((p) => p.bp.sys),
          borderColor: t.pink,
          backgroundColor: withAlpha(t.pink, 0.12),
          fill: false,
          tension: 0.35,
          pointRadius: 4,
          pointHoverRadius: 6,
          pointBackgroundColor: t.pink,
        },
        {
          label: "Tâm trương",
          data: points.map((p) => p.bp.dia),
          borderColor: t.blue,
          backgroundColor: withAlpha(t.blue, 0.12),
          fill: false,
          tension: 0.35,
          pointRadius: 4,
          pointHoverRadius: 6,
          pointBackgroundColor: t.blue,
        },
      ],
    };
    const scales = axisTheme(t);
    scales.y.suggestedMin = 50;
    scales.y.suggestedMax = 160;
    const chartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: true, position: "bottom", labels: { color: t.ink, boxWidth: 12, font: { size: 11 } } },
        tooltip: { callbacks: { label: (ctx) => `${ctx.dataset.label}: ${ctx.raw} mmHg` } },
      },
      scales,
    };
    return { data: chartData, options: chartOptions };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [points, themeV]);

  if (points.length < 2) return null;

  return (
    <div style={{ height, position: "relative" }}>
      <Line data={data} options={options} />
    </div>
  );
}
