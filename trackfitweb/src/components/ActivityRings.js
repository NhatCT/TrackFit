// src/components/ActivityRings.js
// Vòng Activity kiểu Apple Health — nhiều lớp đồng tâm, mỗi vòng một chỉ số.
// Màu lấy từ token (truyền qua prop). Tôn trọng prefers-reduced-motion nhờ rule
// global trong tokens.css.
export default function ActivityRings({ rings = [], size = 132 }) {
  const cx = size / 2;
  const cy = size / 2;
  const sw = 11; // độ dày vòng
  const gap = 3; // khoảng cách giữa các vòng
  const radii = rings.map((_, i) => size / 2 - 8 - i * (sw + gap));

  return (
    <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 16, flexWrap: "wrap" }}>
      <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} style={{ flex: "none" }}>
        <g fill="none" strokeLinecap="round" strokeWidth={sw}>
          {rings.map((r, i) => {
            const radius = radii[i];
            const circ = 2 * Math.PI * radius;
            const pct = Math.max(0, Math.min(1, (r.value || 0) / (r.max || 1)));
            const offset = circ * (1 - pct);
            return (
              <g key={i}>
                {/* rãnh nền = màu vòng mờ (dùng style để var() giải được) */}
                <circle cx={cx} cy={cy} r={radius} style={{ stroke: r.color, opacity: 0.18 }} />
                {/* phần đã hoàn thành */}
                <circle
                  cx={cx}
                  cy={cy}
                  r={radius}
                  strokeDasharray={circ}
                  strokeDashoffset={offset}
                  transform={`rotate(-90 ${cx} ${cy})`}
                  style={{ stroke: r.color, transition: "stroke-dashoffset .8s ease" }}
                />
              </g>
            );
          })}
        </g>
      </svg>

      <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
        {rings.map((r, i) => (
          <div key={i} style={{ display: "flex", flexDirection: "column" }}>
            <span style={{ fontSize: 12.5, fontWeight: 600, color: "var(--muted)" }}>{r.label}</span>
            <span
              className="g-round g-num"
              style={{ fontWeight: 800, fontSize: "1.25rem", letterSpacing: "-.01em", color: r.color }}
            >
              {r.display ?? r.value}
              {r.unit ? <small style={{ fontSize: ".7em", marginLeft: 3 }}>{r.unit}</small> : null}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
