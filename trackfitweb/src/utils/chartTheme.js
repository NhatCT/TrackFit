// src/utils/chartTheme.js
// Đọc màu token thật từ CSS để Chart.js (và các thành phần khác) luôn khớp
// theme sáng/tối, thay cho việc hardcode hex. Nguồn màu duy nhất: tokens.css.
import { useEffect, useState } from "react";

/** Đọc bộ màu token hiện hành từ :root */
export function readTokens() {
  const s = getComputedStyle(document.documentElement);
  const g = (name, fallback) => (s.getPropertyValue(name) || fallback).trim();
  return {
    ink: g("--ink", "#1C1C1E"),
    muted: g("--muted", "#8E8E93"),
    hair: g("--hair", "#E5E5EA"),
    surface: g("--surface", "#FFFFFF"),
    brand: g("--brand", "#F2612C"),
    green: g("--green", "#34C759"),
    blue: g("--blue", "#0A84FF"),
    pink: g("--pink", "#FF2D55"),
    cyan: g("--cyan", "#00C7BE"),
    purple: g("--purple", "#AF52DE"),
    amber: g("--amber", "#FF9500"),
    danger: g("--danger", "#FF3B30"),
  };
}

/** Chuyển một màu (#hex hoặc rgb()) sang rgba() với alpha cho trước */
export function withAlpha(color, alpha) {
  const c = (color || "").trim();
  if (c.startsWith("#")) {
    let hex = c.slice(1);
    if (hex.length === 3) hex = hex.split("").map((x) => x + x).join("");
    const n = parseInt(hex, 16);
    const r = (n >> 16) & 255;
    const g = (n >> 8) & 255;
    const b = n & 255;
    return `rgba(${r},${g},${b},${alpha})`;
  }
  if (c.startsWith("rgb")) {
    const nums = c.replace(/rgba?\(|\)/g, "").split(",").slice(0, 3).map((x) => x.trim());
    return `rgba(${nums.join(",")},${alpha})`;
  }
  return c;
}

/** Cấu hình trục (grid/tick) dùng chung cho Chart.js, màu theo token */
export function axisTheme(t) {
  return {
    x: {
      grid: { display: false },
      ticks: { color: withAlpha(t.muted, 0.9), maxRotation: 0, font: { size: 9 } },
    },
    y: {
      beginAtZero: false,
      grid: { color: withAlpha(t.muted, 0.18) },
      ticks: { color: withAlpha(t.muted, 0.9), font: { size: 9 } },
    },
  };
}

/**
 * Trả về một số đếm tăng mỗi khi theme đổi (data-theme/data-bs-theme trên <html>
 * hoặc prefers-color-scheme). Dùng làm dependency để buộc chart tính lại màu.
 */
export function useThemeVersion() {
  const [v, setV] = useState(0);
  useEffect(() => {
    const bump = () => setV((x) => x + 1);
    const obs = new MutationObserver(bump);
    obs.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ["data-theme", "data-bs-theme"],
    });
    const mq = window.matchMedia("(prefers-color-scheme: dark)");
    mq.addEventListener?.("change", bump);
    return () => {
      obs.disconnect();
      mq.removeEventListener?.("change", bump);
    };
  }, []);
  return v;
}
