// src/components/ChatWidget.jsx
import React from "react";
import cookie from "react-cookies";
import { authApis, endpoints } from "../configs/Apis";

import gutimAvatar from "../img/gutim-bot.jpg";

function getSessionId() {
  const saved = localStorage.getItem("tf_chat_session");
  if (saved) return saved;
  const sid = "s-" + Date.now().toString(36) + "-" + Math.random().toString(36).slice(2, 8);
  localStorage.setItem("tf_chat_session", sid);
  return sid;
}

const STYLE = `
/* FAB */
.gutim-fab { position:fixed; right:24px; bottom:24px; z-index:9999;
  width:64px; height:64px; border-radius:50%; border:none; cursor:pointer;
  color:#fff; font-size:24px; display:flex; align-items:center; justify-content:center;
  background:conic-gradient(from 180deg, #2563eb, #7c3aed, #2563eb);
  box-shadow:0 14px 36px rgba(0,0,0,.34);
  animation:gutim-rotate 6s linear infinite;
}
@keyframes gutim-rotate { to { transform: rotate(360deg) } }
.gutim-fab-inner {
  width:58px; height:58px; border-radius:50%; background:#111827;
  display:flex; align-items:center; justify-content:center;
}

/* Panel */
.gutim-panel { position:fixed; right:24px; bottom:100px; z-index:9999;
  width:380px; max-width:calc(100vw - 32px); height:520px; display:flex; flex-direction:column;
  background: #0b1020; border:1px solid rgba(255,255,255,.06); border-radius:18px;
  overflow:hidden; box-shadow:0 16px 48px rgba(0,0,0,.45);
}

/* Header */
.gutim-hd { height:64px; display:flex; align-items:center; gap:10px; padding:10px 12px;
  background:linear-gradient(135deg, rgba(37,99,235,.12), rgba(124,58,237,.12));
  border-bottom:1px solid rgba(255,255,255,.06);
}
.gutim-avatar { width:42px; height:42px; border-radius:50%; overflow:hidden; position:relative; background:#0b1020; border:1px solid rgba(255,255,255,.08) }
.gutim-avatar img { width:100%; height:100%; object-fit:cover }
.gutim-dot { position:absolute; right:-2px; bottom:-2px; width:12px; height:12px; border-radius:50%;
  background:#22c55e; box-shadow:0 0 0 2px #0b1020 inset; }
.gutim-title { display:flex; flex-direction:column; }
.gutim-name { font-weight:800; color:#fff; font-size:14px; letter-spacing:.3px }
.gutim-sub { font-size:12px; color:#a3a3a3 }

.gutim-close { margin-left:auto; border:none; background:transparent; color:#9ca3af; cursor:pointer; font-size:18px }

/* Body */
.gutim-body { flex:1; padding:12px; overflow-y:auto; background:
 radial-gradient(1200px 600px at 120% -20%, rgba(124,58,237,.20), transparent 60%),
 radial-gradient(800px 500px at -20% 120%, rgba(37,99,235,.20), transparent 60%),
 linear-gradient(#0b1020, #0b1020); }
.gutim-msg { max-width:78%; padding:10px 12px; border-radius:14px; margin:8px 0; white-space:pre-wrap; line-height:1.45; font-size:14px; color:#e5e7eb }
.gutim-msg.user { margin-left:auto; background:linear-gradient(135deg, #2563eb, #7c3aed); color:#fff; border-bottom-right-radius:6px; }
.gutim-msg.bot  { margin-right:auto; background:rgba(255,255,255,.06); border:1px solid rgba(255,255,255,.08); border-bottom-left-radius:6px; backdrop-filter: blur(4px); }

/* Footer */
.gutim-ft { padding:10px; border-top:1px solid rgba(255,255,255,.06); background:rgba(255,255,255,.02); display:flex; gap:8px }
.gutim-input { flex:1; min-height:44px; max-height:140px; resize:none; border-radius:12px; padding:10px 12px; outline:none; font-size:14px;
  border:1px solid rgba(255,255,255,.08); background:#0f172a; color:#e5e7eb }
.gutim-send { min-width:92px; border:none; border-radius:12px; background:linear-gradient(135deg, #2563eb, #7c3aed); color:#fff; font-weight:800; cursor:pointer; padding:0 12px }
.gutim-send:disabled { opacity:.55; cursor:not-allowed }

.gutim-tip { font-size:12px; color:#9ca3af; padding: 6px 12px }
`;

function FallbackChibi() {
  // Fallback SVG nếu bạn chưa có ảnh gutim-chibi.png
  return (
    <svg viewBox="0 0 64 64" width="64" height="64" aria-hidden="true">
      <circle cx="32" cy="32" r="30" fill="#111827" stroke="#7c3aed" strokeWidth="3" />
      <circle cx="24" cy="26" r="6" fill="#ffb6b6" />
      <circle cx="40" cy="26" r="6" fill="#ffb6b6" />
      <rect x="20" y="36" width="24" height="10" rx="5" fill="#2563eb" />
      <path d="M13 45 C20 35, 44 35, 51 45" stroke="#7c3aed" strokeWidth="3" fill="none" />
      <path d="M28 22 h8" stroke="#333" strokeWidth="3" />
    </svg>
  );
}

export default function ChatWidget({ requireAuth = false }) {
  const token = cookie.load("token");
  const authed = !!token || !requireAuth;

  const sessionId = React.useMemo(() => getSessionId(), []);
  const storageKey = React.useMemo(() => `tf_chat_msgs_${sessionId}`, [sessionId]);

  const [open, setOpen] = React.useState(false);
  const [statusOk, setStatusOk] = React.useState(true);
  const [msgs, setMsgs] = React.useState(() => {
    const saved = localStorage.getItem(storageKey);
    return saved ? JSON.parse(saved) : [{ role: "bot", text: "Xin chào! Mình là Gutim Coach AI 🤖💪 — hỏi mình về bài tập, dinh dưỡng hay lịch tập nhé!" }];
  });
  const [input, setInput] = React.useState("");
  const [sending, setSending] = React.useState(false);
  const [model, setModel] = React.useState("");
  const bodyRef = React.useRef(null);

  // Health check
  React.useEffect(() => {
    let mounted = true;
    const ping = () =>
      authApis().get(endpoints.aiHealth)
        .then(r => mounted && setStatusOk(!!r?.data?.ok))
        .catch(() => mounted && setStatusOk(false));
    ping();
    const iv = setInterval(ping, 30000);
    return () => { mounted = false; clearInterval(iv); };
  }, []);

  // Persist & autoscroll
  React.useEffect(() => {
    const trimmed = msgs.length > 120 ? msgs.slice(msgs.length - 120) : msgs;
    if (trimmed.length !== msgs.length) setMsgs(trimmed);
    localStorage.setItem(storageKey, JSON.stringify(trimmed));
    if (open && bodyRef.current) bodyRef.current.scrollTop = bodyRef.current.scrollHeight;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [msgs, open, storageKey]);

  const send = async () => {
    const q = input.trim();
    if (!q || sending) return;
    setInput("");
    setMsgs(prev => [...prev, { role: "user", text: q }]);
    setSending(true);
    try {
      const { data } = await authApis().post(endpoints.aiChatAsk, { sessionId, question: q, topK: 4 });
      setMsgs(prev => [...prev, { role: "bot", text: data?.answer || "Mình chưa có câu trả lời." }]);
      setModel(data?.model || "");
    } catch (e) {
      const msg = e?.response?.status === 401
        ? "Bạn cần đăng nhập để dùng chatbot."
        : `Có lỗi khi gọi chatbot${e?.response?.status ? ` (${e.response.status})` : ""}. Vui lòng thử lại.`;
      setMsgs(prev => [...prev, { role: "bot", text: msg }]);
    } finally {
      setSending(false);
    }
  };

  const onKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      send();
    }
  };

  return (
    <>
      <style dangerouslySetInnerHTML={{ __html: STYLE }} />

      <button className="gutim-fab" aria-label="Mở Chatbot" onClick={() => setOpen(v => !v)}>
        <div className="gutim-fab-inner">💬</div>
      </button>

      {open && (
        <div className="gutim-panel">
          <div className="gutim-hd">
            <div className="gutim-avatar">
              {gutimAvatar ? <img src={gutimAvatar} alt="Gutim Coach AI" /> : <FallbackChibi />}
              <span className="gutim-dot" style={{ background: statusOk ? "#22c55e" : "#ef4444" }} />
            </div>
            <div className="gutim-title">
              <div className="gutim-name">Gutim Coach AI</div>
              <div className="gutim-sub">{statusOk ? "Sẵn sàng hỗ trợ" : "Mất kết nối AI"}</div>
            </div>
            <button className="gutim-close" onClick={() => setOpen(false)} title="Đóng">✕</button>
          </div>

          <div className="gutim-body" ref={bodyRef}>
            {!authed && (
              <div className="gutim-msg bot">
                Vui lòng <a href="/login" style={{ color:"#93c5fd", fontWeight:800 }}>đăng nhập</a> để sử dụng chatbot.
              </div>
            )}
            {msgs.map((m, i) => (
              <div key={i} className={`gutim-msg ${m.role === "user" ? "user" : "bot"}`}>
                {m.text}
              </div>
            ))}
            {sending && <div className="gutim-tip">Đang soạn trả lời…</div>}
          </div>

          <div className="gutim-ft">
            <textarea
              className="gutim-input"
              disabled={!authed}
              placeholder={authed ? "Nhập tin nhắn... (Enter gửi, Shift+Enter xuống dòng)" : "Hãy đăng nhập để chat"}
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={onKeyDown}
            />
            <button className="gutim-send" disabled={!authed || sending || !input.trim()} onClick={send}>
              Gửi
            </button>
          </div>

          {!!model && (
            <div className="gutim-tip">
              model: {model}
            </div>
          )}
        </div>
      )}
    </>
  );
}
