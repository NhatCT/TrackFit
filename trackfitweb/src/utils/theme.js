// Quản lý theme sáng/tối cho Gutim.
// Áp đồng thời data-theme (token của app) và data-bs-theme (Bootstrap 5.3)
// lên <html>, và lưu lựa chọn vào localStorage.

const KEY = "gutim-theme"; // "light" | "dark" | (không có = theo hệ điều hành)

export function storedTheme() {
  try {
    return localStorage.getItem(KEY);
  } catch {
    return null;
  }
}

export function systemPrefersDark() {
  return typeof window !== "undefined"
    && window.matchMedia
    && window.matchMedia("(prefers-color-scheme: dark)").matches;
}

// Theme đang hiển thị thực tế ("light" | "dark")
export function effectiveTheme() {
  const s = storedTheme();
  if (s === "light" || s === "dark") return s;
  return systemPrefersDark() ? "dark" : "light";
}

// Áp một theme cụ thể lên <html>. Truyền null để quay về "theo hệ điều hành".
export function applyTheme(theme) {
  const root = document.documentElement;
  const eff = theme === "light" || theme === "dark"
    ? theme
    : (systemPrefersDark() ? "dark" : "light");

  if (theme === "light" || theme === "dark") {
    root.setAttribute("data-theme", theme);
  } else {
    root.removeAttribute("data-theme");
  }
  // Đồng bộ Bootstrap để card/table/dropdown/form tự đổi màu
  root.setAttribute("data-bs-theme", eff);
}

// Khởi tạo sớm (gọi trước khi render để tránh nháy màu).
// Mặc định: giữ giao diện tối hiện có nếu người dùng chưa từng chọn.
export function initTheme() {
  let s = storedTheme();
  if (s !== "light" && s !== "dark") {
    s = "dark"; // mặc định an toàn, không đổi trải nghiệm cũ
  }
  applyTheme(s);
  return s;
}

// Chuyển đổi và lưu lựa chọn. Trả về theme mới.
export function toggleTheme() {
  const next = effectiveTheme() === "dark" ? "light" : "dark";
  try {
    localStorage.setItem(KEY, next);
  } catch {
    /* ignore */
  }
  applyTheme(next);
  return next;
}
