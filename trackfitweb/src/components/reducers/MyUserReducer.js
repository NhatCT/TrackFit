// src/components/reducers/MyUserReducer.js
import cookie from "react-cookies";

/** Đồng bộ cookie "user" để giữ trạng thái sau reload */
function saveUserToCookie(user) {
  try {
    if (!user) cookie.remove("user", { path: "/" });
    else cookie.save("user", user, { path: "/" });
  } catch {
    // ignore
  }
}

export default function MyUserReducer(current, action) {
  switch (action.type) {
    case "login": {
      const next = action.payload || null;
      if (next) saveUserToCookie(next); // lưu user vào cookie
      return next;
    }

    case "logout": {
      cookie.remove("user", { path: "/" });
      cookie.remove("token", { path: "/" });
      return null;
    }

    // Cập nhật chỉ ảnh đại diện -> thêm avatarVersion để bust cache
    case "updateAvatar": {
      if (!current) return current; // chưa đăng nhập
      const next = {
        ...current,
        avatarUrl: action.payload,     // URL mới từ server
        avatarVersion: Date.now(),     // bump version => ép browser tải lại
      };
      saveUserToCookie(next);
      return next;
    }

    // Cập nhật nhiều trường hồ sơ (firstName, lastName, email, gender, ...)
    case "updateProfile": {
      if (!current) return current;
      const next = { ...current, ...action.payload };
      saveUserToCookie(next);
      return next;
    }

    default:
      return current;
  }
}
