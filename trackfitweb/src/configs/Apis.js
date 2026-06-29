// src/configs/Apis.js
import axios from "axios";
import cookie from "react-cookies";

const BASE_URL = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080/TrackFit/api/";

/** Lấy headers xác thực hiện tại (nếu có token trong cookie) */
const buildAuthHeaders = () => {
  const token = cookie.load("token");
  const headers = { "Content-Type": "application/json" };
  if (token) headers["Authorization"] = `Bearer ${token}`;
  return headers;
};

export const endpoints = {
  // ==== Auth / Users (secure) ====
  login: "login",
  register: "register",
  profile: (username) => (username ? `secure/profile/${username}` : "secure/profile"),
  changePassword: "secure/password",
  changeAvatar: "secure/avatar",

  // ==== Exercises ====
  exercises: "secure/exercises",
  exerciseDetail: (id) => `secure/exercises/${id}`,

  // ==== Goals ====
  goals: "secure/goals",
  goalDetail: (id) => `secure/goals/${id}`,

  // ==== Health ====
  health: "secure/health",
  healthDetail: (id) => `secure/health/${id}`,

  // ==== Notifications ====
 notifications: "secure/notifications",
  notificationDetail: (id) => `secure/notifications/${id}`,
  notificationMarkRead: (id, value) =>
    `secure/notifications/${id}/read?value=${value}`,
  unreadCount: "secure/notifications/unread-count",
  readAll: "secure/notifications/read-all",               // PUT
  cleanup: (olderThanDays) =>
    `secure/notifications/cleanup?olderThanDays=${olderThanDays}`,
  // ==== AI Advice (ADMIN) ====
  // Gửi yêu cầu AI gợi ý lời khuyên cho người dùng
  aiAdviceFromReco: (username, top = 3, withinDays = 1) =>
    `admin/notifications/ai/for-user?username=${encodeURIComponent(username)}&top=${top}&withinDays=${withinDays}`,
// ==== Admin Users ====
adminUsers: (kw = "", limit = 100) =>
  `admin/users?kw=${encodeURIComponent(kw)}&limit=${limit}`,

  // ==== Recommendations ====
  recommendations: "secure/recommendations",
  recommendationsAuto: "secure/recommendations/auto",
  
  // ==== Subscription / PRO ====
  subscriptionStatus: "secure/subscription/status",
  subscriptionConfirm: "secure/subscription/confirm",

  // ==== AI Services ====
  aiHealth: "secure/ai/health",
  aiReindex: "secure/ai/reindex",
  aiChatAsk: "secure/ai/chat/ask",


  // ==== Stats ====
  statsSummary: "secure/stats/summary",

  // ==== Plans ====
  plans: "secure/plans",
  planDetail: (id) => `secure/plans/${id}`,
  planAddDetail: (id) => `secure/plans/${id}/details`,
  planDetailUpdate: (detailId) => `secure/plans/details/${detailId}`,
  planDetailDelete: (detailId) => `secure/plans/details/${detailId}`,

  // ==== Histories ====
  histories: "secure/histories",
  historyDetail: (id) => `secure/histories/${id}`,
};

/** Instance cho các request cần Bearer token */
export const authApis = () =>
  axios.create({
    baseURL: BASE_URL,
    headers: buildAuthHeaders(),
    withCredentials: false, // bật true nếu backend dùng cookie-session
  });

/** Instance chung (không cần Bearer token) */
export default axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "application/json" },
});
