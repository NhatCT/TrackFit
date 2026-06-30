const YT_ID_RE = [
  /[?&]v=([A-Za-z0-9_-]{11})/,
  /youtu\.be\/([A-Za-z0-9_-]{11})/,
  /\/embed\/([A-Za-z0-9_-]{11})/,
];

export const extractYoutubeId = (url) => {
  if (!url) return null;
  for (const re of YT_ID_RE) {
    const m = url.match(re);
    if (m) return m[1];
  }
  return null;
};

export const getYoutubeThumbnail = (url) => {
  const id = extractYoutubeId(url);
  return id ? `https://i.ytimg.com/vi/${id}/hqdefault.jpg` : null;
};

export const toHttpUrl = (url) => {
  if (!url) return "#";
  const s = url.trim();
  if (/^https?:\/\//i.test(s)) return s;
  if (s.startsWith("//")) return `https:${s}`;
  return `https://${s}`;
};
