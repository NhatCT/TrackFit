import React, { useContext } from "react";
import { authApis, endpoints } from "../configs/Apis";
import { MyUserContext } from "../configs/Context";

export default function Chatbot() {
  const [user] = useContext(MyUserContext);
  const sessionId = user?.username || "guest";

  const [status, setStatus] = React.useState("Đang kiểm tra kết nối…");
  const [question, setQuestion] = React.useState("");
  const [answer, setAnswer] = React.useState("");
  const [model, setModel] = React.useState("");
  const [loading, setLoading] = React.useState(false);

  React.useEffect(() => {
    let mounted = true;
    authApis().get(endpoints.aiHealth)
      .then(res => mounted && setStatus(res?.data?.ok ? "Sẵn sàng" : "Mất kết nối"))
      .catch(() => mounted && setStatus("Mất kết nối"));
    return () => { mounted = false; };
  }, []);

  const ask = async () => {
    const q = question.trim();
    if (!q || loading) return;
    setLoading(true);
    setAnswer("...");
    try {
      const { data } = await authApis().post(endpoints.aiChatAsk, { sessionId, question: q, topK: 4 });
      setAnswer(data?.answer || "(không có trả lời)");
      setModel(data?.model || "");
    } catch (e) {
      setAnswer(`Lỗi gửi tin nhắn${e?.response?.status ? ` (${e.response.status})` : ""}`);
      setModel("");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2 className="h5 fw-semibold">Trợ lý sức khỏe</h2>
        <span className="text-muted small">{status}</span>
      </div>

      <div className="d-flex gap-2 mb-3">
        <input
          className="form-control"
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          onKeyDown={(e) => { if (e.key === "Enter") ask(); }}
          placeholder="Hỏi về bài tập, lịch, dinh dưỡng..."
        />
        <button className="btn btn-primary" onClick={ask} disabled={!question.trim() || loading}>
          {loading ? "Đang hỏi..." : "Hỏi"}
        </button>
      </div>

      <div className="border rounded p-3" style={{ minHeight: 120, whiteSpace: "pre-wrap" }}>
        {answer || "Gõ câu hỏi và bấm Hỏi"}
      </div>
      {model && <div className="text-muted small mt-2">model: {model}</div>}
    </div>
  );
}
