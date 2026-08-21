// Nút chuyển sáng/tối cho thanh header.
import { useEffect, useState } from "react";
import { effectiveTheme, toggleTheme } from "../../utils/theme";

const ThemeToggle = () => {
  const [theme, setTheme] = useState(() => effectiveTheme());

  // Đồng bộ nếu hệ điều hành đổi và người dùng đang ở chế độ "theo hệ thống"
  useEffect(() => {
    if (!window.matchMedia) return;
    const mq = window.matchMedia("(prefers-color-scheme: dark)");
    const onChange = () => setTheme(effectiveTheme());
    mq.addEventListener?.("change", onChange);
    return () => mq.removeEventListener?.("change", onChange);
  }, []);

  const onClick = () => setTheme(toggleTheme());
  const isDark = theme === "dark";

  return (
    <button
      type="button"
      onClick={onClick}
      title={isDark ? "Chuyển sang chế độ sáng" : "Chuyển sang chế độ tối"}
      aria-label={isDark ? "Chuyển sang chế độ sáng" : "Chuyển sang chế độ tối"}
      style={{
        width: 38,
        height: 38,
        borderRadius: 12,
        display: "grid",
        placeItems: "center",
        border: "1px solid rgba(255,255,255,.16)",
        background: "rgba(255,255,255,.06)",
        color: "#fff",
        cursor: "pointer",
      }}
    >
      {isDark ? (
        // mặt trời
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
          <circle cx="12" cy="12" r="4" />
          <path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" />
        </svg>
      ) : (
        // mặt trăng
        <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
          <path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z" />
        </svg>
      )}
    </button>
  );
};

export default ThemeToggle;
