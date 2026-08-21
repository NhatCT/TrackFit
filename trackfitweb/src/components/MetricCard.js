// src/components/MetricCard.js
// Thẻ chỉ số tái dùng (icon-tile màu danh mục + nhãn + số lớn Nunito + đơn vị +
// dòng phụ). Dựng trên helper token có sẵn: .g-card / .g-value / .g-ic--*.
const NAME_COLOR = {
  brand: "var(--brand)",
  purple: "var(--purple)",
  green: "var(--green)",
  pink: "var(--pink)",
  blue: "var(--blue)",
};

export default function MetricCard({
  color = "brand",
  icon,
  name,
  value,
  unit,
  sub,
  className = "",
  style,
}) {
  return (
    <div className={`g-card h-100 ${className}`} style={style}>
      <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 14 }}>
        {icon ? <span className={`g-ic g-ic--${color}`}>{icon}</span> : null}
        <span style={{ fontSize: 15, fontWeight: 700, color: NAME_COLOR[color] || "var(--ink)" }}>
          {name}
        </span>
      </div>
      <div>
        <span className="g-value g-num">
          {value}
          {unit ? <span className="g-unit">{unit}</span> : null}
        </span>
      </div>
      {sub ? (
        <div style={{ color: "var(--muted)", fontSize: ".85rem", marginTop: 10, fontWeight: 500 }}>
          {sub}
        </div>
      ) : null}
    </div>
  );
}
