/** Ép số an toàn */
export const toInt = (v, fallback = null) => {
  if (v === "" || v === null || v === undefined) return fallback;
  const n = Number(v);
  return Number.isFinite(n) ? n : fallback;
};

/** Hiển thị % đẹp mắt (0..1 -> %, 0..100 -> %) */
export const formatScore = (raw) => {
  if (raw == null) return null;
  const pct = raw <= 1 ? Math.round(raw * 100) : Math.round(raw);
  return `${pct}%`;
};

/** Tạo key ổn định cho item (không dùng random) */
export const getKey = (item) =>
  item?.exerciseId?.toString?.() ??
  item?.idxKey?.toString?.() ??
  item?.name ??
  "unknown";

/** Chuẩn hoá dữ liệu từ API */
export const normalizeItem = (x, idx) => ({
  idxKey: `idx-${idx}`, // fallback key
  exerciseId: x.exerciseId ?? x.id ?? x.exercisesId ?? null,
  name: x.name ?? x.exerciseName ?? "Bài tập",
  estimatedMinutes: x.estimatedMinutes ?? x.minutes ?? null,
  description: x.description ?? x.desc ?? "",
  muscleGroup: x.muscleGroup ?? x.type ?? x.muscle ?? "",
  difficulty: x.difficulty ?? x.intensity ?? "",
  score: x.score ?? null,        // 0..1 hoặc 0..100
  reason: x.reason ?? "",         // lý do từ AI (nếu có)
});

/** Build query params “sạch” để gọi API gợi ý */
export const buildQueryParams = (params) => {
  const qp = {};
  const size = toInt(params.size, 8);
  if (size) qp.size = size;

  const minutes = toInt(params.availableMinutes, null);
  if (minutes !== null) qp.availableMinutes = minutes;

  if (params.kw?.trim()) qp.kw = params.kw.trim();
  if (params.intensity?.trim()) qp.intensity = params.intensity.trim();
  if (params.goalType?.trim()) qp.goalType = params.goalType.trim();

  return qp;
};
