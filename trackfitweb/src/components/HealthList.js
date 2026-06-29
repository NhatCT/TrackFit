// src/components/HealthList.js
import { useEffect, useMemo, useState } from "react";
import { Alert, Badge, Button, Card, Form, InputGroup, Table } from "react-bootstrap";
import { authApis, endpoints } from "../configs/Apis";
import MySpinner from "./layout/MySpinner";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Filler,
  Tooltip,
  Legend,
} from "chart.js";
import { Line } from "react-chartjs-2";

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Filler, Tooltip, Legend);

const EMPTY = { height: "", weight: "", bloodPressure: "", notes: "" };

const bpOk = (s) => !s || /^\d{2,3}\/\d{2,3}$/.test(s.trim()); // 120/80
const toNum = (v) => (v === "" || v === null ? null : Number(v));

const bmiCalc = (heightCm, weightKg) => {
  const h = Number(heightCm), w = Number(weightKg);
  if (!h || !w) return { bmi: null, label: "-" };
  const bmi = +(w / Math.pow(h / 100, 2)).toFixed(1);
  let label = "Bình thường";
  if (bmi < 18.5) label = "Thiếu cân";
  else if (bmi >= 23 && bmi < 25) label = "Thừa cân";
  else if (bmi >= 25) label = "Béo phì";
  return { bmi, label };
};

const fmtDate = (d) => {
  if (!d) return "";
  const dt = new Date(d);
  return `${String(dt.getDate()).padStart(2, "0")}/${String(dt.getMonth() + 1).padStart(2, "0")}`;
};

const HealthList = () => {
  const [items, setItems] = useState([]);
  const [form, setForm] = useState(EMPTY);
  const [kw, setKw] = useState("");
  const [msg, setMsg] = useState({ type: "", text: "" });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [chartMode, setChartMode] = useState("weight"); // "weight" | "bmi"

  const load = async () => {
    setLoading(true);
    try {
      const r = await authApis().get(endpoints.health);
      const data = Array.isArray(r.data) ? r.data : (r.data?.items || []);
      // sắp xếp mới nhất trước (nếu BE chưa sort)
      setItems([...data].sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0)));
    } catch (e) {
      setMsg({ type: "danger", text: e?.response?.data?.message || "Tải dữ liệu thất bại" });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const filtered = useMemo(() => {
    if (!kw.trim()) return items;
    const q = kw.toLowerCase();
    return items.filter((x) =>
      (x.notes || "").toLowerCase().includes(q) ||
      (x.bloodPressure || "").toLowerCase().includes(q)
    );
  }, [items, kw]);

  // ===== CHART DATA =====
  // Sort ascending by date for chart
  const sortedAsc = useMemo(() =>
    [...items].sort((a, b) => new Date(a.createdAt || 0) - new Date(b.createdAt || 0)),
    [items]
  );

  const chartLabels = useMemo(() => sortedAsc.map((h) => fmtDate(h.createdAt)), [sortedAsc]);

  const weightData = useMemo(() => sortedAsc.map((h) => Number(h.weight) || null), [sortedAsc]);

  const bmiData = useMemo(
    () => sortedAsc.map((h) => bmiCalc(h.height, h.weight).bmi),
    [sortedAsc]
  );

  const latestBmi = bmiData.length ? bmiData[bmiData.length - 1] : null;
  const bmiAlert = latestBmi != null && (latestBmi < 18.5 || latestBmi >= 25);

  const chartDataWeight = {
    labels: chartLabels,
    datasets: [
      {
        label: "Cân nặng (kg)",
        data: weightData,
        borderColor: "#4cc9f0",
        backgroundColor: "rgba(76,201,240,0.12)",
        fill: true,
        tension: 0.35,
        pointRadius: 4,
        pointHoverRadius: 6,
        pointBackgroundColor: "#4cc9f0",
      },
    ],
  };

  const chartDataBmi = {
    labels: chartLabels,
    datasets: [
      {
        label: "BMI",
        data: bmiData,
        borderColor: "#ff6b35",
        backgroundColor: "rgba(255,107,53,0.12)",
        fill: true,
        tension: 0.35,
        pointRadius: 4,
        pointHoverRadius: 6,
        pointBackgroundColor: "#ff6b35",
      },
    ],
  };

  const chartOptions = (mode) => ({
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        callbacks: {
          label: (ctx) => {
            if (mode === "bmi") {
              const v = ctx.raw;
              const lbl = v < 18.5 ? "Thiếu cân" : v < 23 ? "Bình thường" : v < 25 ? "Thừa cân" : "Béo phì";
              return `BMI: ${v} (${lbl})`;
            }
            return `${ctx.raw} kg`;
          },
        },
      },
    },
    scales: {
      x: {
        grid: { color: "rgba(255,255,255,0.06)" },
        ticks: { color: "rgba(255,255,255,0.65)", maxRotation: 0 },
      },
      y: {
        beginAtZero: false,
        grid: { color: "rgba(255,255,255,0.06)" },
        ticks: { color: "rgba(255,255,255,0.65)" },
        ...(mode === "bmi"
          ? {
              suggestedMin: 14,
              suggestedMax: 35,
            }
          : {}),
      },
    },
  });

  // ===== FORM HANDLERS =====
  const submit = async (e) => {
    e.preventDefault();
    setMsg({ type: "", text: "" });

    // validate đơn giản
    const height = toNum(form.height);
    const weight = toNum(form.weight);
    if (!height || height < 80 || height > 250) {
      setMsg({ type: "danger", text: "Chiều cao phải trong khoảng 80–250 cm." });
      return;
    }
    if (!weight || weight < 20 || weight > 300) {
      setMsg({ type: "danger", text: "Cân nặng phải trong khoảng 20–300 kg." });
      return;
    }
    if (!bpOk(form.bloodPressure)) {
      setMsg({ type: "danger", text: "Huyết áp không hợp lệ (ví dụ: 120/80)." });
      return;
    }

    setSaving(true);
    try {
      await authApis().post(endpoints.health, {
        height,
        weight,
        bloodPressure: form.bloodPressure?.trim() || "",
        notes: form.notes?.trim() || "",
      });
      setForm(EMPTY);
      setMsg({ type: "success", text: "Đã thêm bản ghi sức khỏe." });
      load();
    } catch (e) {
      setMsg({ type: "danger", text: e?.response?.data?.message || "Lưu thất bại" });
    } finally {
      setSaving(false);
    }
  };

  const del = async (id) => {
    if (!window.confirm("Xóa bản ghi sức khỏe này?")) return;
    try {
      await authApis().delete(`${endpoints.health}/${id}`);
      setMsg({ type: "success", text: "Đã xóa bản ghi." });
      load();
    } catch (e) {
      setMsg({ type: "danger", text: e?.response?.data?.message || "Xóa thất bại" });
    }
  };

  const { bmi, label } = bmiCalc(form.height, form.weight);

  return (
    <>
      <div className="d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-3 mb-3">
        <div className="d-flex align-items-center gap-2">
          <h3 className="m-0">Sức khỏe</h3>
          {!!items?.length && <Badge bg="secondary">{items.length}</Badge>}
        </div>

        <Form
          className="d-flex gap-2"
          onSubmit={(e) => e.preventDefault()}
        >
          <Form.Control
            className="search-pill"
            placeholder="Tìm theo ghi chú hoặc huyết áp…"
            value={kw}
            onChange={(e) => setKw(e.target.value)}
            style={{ minWidth: 260 }}
          />
          <Button variant="outline-secondary" onClick={() => setKw("")}>
            Xoá lọc
          </Button>
        </Form>
      </div>

      {/* ===== BMI Alert ===== */}
      {bmiAlert && latestBmi != null && (
        <Alert variant={latestBmi < 18.5 ? "info" : "warning"} className="d-flex align-items-center gap-2">
          <span>
            {latestBmi < 18.5
              ? `⚠️ BMI hiện tại của bạn là ${latestBmi} (Thiếu cân). Hãy xem xét tăng cường dinh dưỡng.`
              : `⚠️ BMI hiện tại của bạn là ${latestBmi} (${latestBmi >= 25 ? "Béo phì" : "Thừa cân"}). Hãy điều chỉnh chế độ tập luyện.`
            }
          </span>
          <Button size="sm" variant="outline-primary" href="/recommendations" className="ms-auto text-nowrap">
            Xem gợi ý cho bạn
          </Button>
        </Alert>
      )}

      {/* ===== TREND CHART ===== */}
      {sortedAsc.length >= 2 && (
        <Card className="shadow-sm border-0 mb-3" data-aos="fade-up">
          <Card.Body>
            <div className="d-flex justify-content-between align-items-center mb-3">
              <div className="text-muted small text-uppercase fw-semibold">
                {chartMode === "weight" ? "Xu hướng cân nặng" : "Xu hướng BMI"}
              </div>
              <div className="d-flex gap-1">
                <Button
                  size="sm"
                  variant={chartMode === "weight" ? "primary" : "outline-secondary"}
                  onClick={() => setChartMode("weight")}
                >
                  Cân nặng
                </Button>
                <Button
                  size="sm"
                  variant={chartMode === "bmi" ? "primary" : "outline-secondary"}
                  onClick={() => setChartMode("bmi")}
                >
                  BMI
                </Button>
              </div>
            </div>
            <div style={{ height: 280, position: "relative" }}>
              <Line
                data={chartMode === "weight" ? chartDataWeight : chartDataBmi}
                options={chartOptions(chartMode)}
              />
            </div>
            {chartMode === "bmi" && (
              <div className="d-flex gap-3 mt-2 flex-wrap" style={{ fontSize: "0.75rem" }}>
                <span><span style={{ color: "#6c757d" }}>●</span> &lt;18.5 Thiếu cân</span>
                <span><span style={{ color: "#198754" }}>●</span> 18.5–22.9 Bình thường</span>
                <span><span style={{ color: "#ffc107" }}>●</span> 23–24.9 Thừa cân</span>
                <span><span style={{ color: "#dc3545" }}>●</span> ≥25 Béo phì</span>
              </div>
            )}
          </Card.Body>
        </Card>
      )}

      {/* Form thêm mới */}
      <Card className="shadow-sm border-0 mb-3 hoverable plan-card">
        <Card.Header className="bg-transparent border-0 pt-3 pb-0"><strong>Thêm bản ghi</strong></Card.Header>
        <Card.Body>
          {msg.text && <Alert variant={msg.type || "info"}>{msg.text}</Alert>}

          <Form onSubmit={submit}>
            <div className="row g-3">
              <div className="col-md-3">
                <Form.Label>Chiều cao</Form.Label>
                <InputGroup>
                  <Form.Control
                    type="number"
                    min={80}
                    max={250}
                    step="0.1"
                    placeholder="Ví dụ: 170"
                    value={form.height}
                    onChange={(e) => setForm({ ...form, height: e.target.value })}
                    required
                  />
                  <InputGroup.Text>cm</InputGroup.Text>
                </InputGroup>
              </div>

              <div className="col-md-3">
                <Form.Label>Cân nặng</Form.Label>
                <InputGroup>
                  <Form.Control
                    type="number"
                    min={20}
                    max={300}
                    step="0.1"
                    placeholder="Ví dụ: 65"
                    value={form.weight}
                    onChange={(e) => setForm({ ...form, weight: e.target.value })}
                    required
                  />
                  <InputGroup.Text>kg</InputGroup.Text>
                </InputGroup>
              </div>

              <div className="col-md-3">
                <Form.Label>Huyết áp</Form.Label>
                <Form.Control
                  placeholder="vd: 120/80"
                  value={form.bloodPressure}
                  onChange={(e) => setForm({ ...form, bloodPressure: e.target.value })}
                  isInvalid={!!form.bloodPressure && !bpOk(form.bloodPressure)}
                />
                <Form.Control.Feedback type="invalid">
                  Định dạng 120/80
                </Form.Control.Feedback>
              </div>

              <div className="col-md-3">
                <Form.Label>Ghi chú</Form.Label>
                <Form.Control
                  placeholder="Ví dụ: sau khi chạy 5km…"
                  value={form.notes}
                  onChange={(e) => setForm({ ...form, notes: e.target.value })}
                />
              </div>

              {/* BMI xem nhanh */}
              <div className="col-12 col-md-6">
                <div className="small text-muted">
                  BMI hiện tại: <strong>{bmi ?? "-"}</strong>{" "}
                  {bmi && (
                    <Badge bg={
                      bmi < 18.5 ? "secondary" :
                      bmi < 23 ? "success" :
                      bmi < 25 ? "warning" : "danger"
                    }>
                      {label}
                    </Badge>
                  )}
                </div>
              </div>

              <div className="col-12 col-md-6 d-grid d-md-flex justify-content-md-end">
                <Button type="submit" disabled={saving}>
                  {saving ? "Đang lưu…" : "Thêm"}
                </Button>
              </div>
            </div>
          </Form>
        </Card.Body>
      </Card>

      {/* Bảng danh sách */}
      {loading ? (
        <MySpinner />
      ) : (
        <Card className="shadow-sm border-0">
          <Table hover responsive className="m-0 align-middle">
            <thead className="table-light">
              <tr>
                <th style={{ width: 90 }}>ID</th>
                <th>Ngày</th>
                <th>Chiều cao</th>
                <th>Cân nặng</th>
                <th>BMI</th>
                <th>Huyết áp</th>
                <th>Ghi chú</th>
                <th style={{ width: 120 }}></th>
              </tr>
            </thead>
            <tbody>
              {filtered?.length ? (
                filtered.map((h) => {
                  const { bmi, label } = bmiCalc(h.height, h.weight);
                  return (
                    <tr key={h.healthId}>
                      <td>#{h.healthId}</td>
                      <td>{h.createdAt ? new Date(h.createdAt).toLocaleString() : "-"}</td>
                      <td>{h.height ? `${h.height} cm` : "-"}</td>
                      <td>{h.weight ? `${h.weight} kg` : "-"}</td>
                      <td>
                        {bmi ? (
                          <>
                            <strong>{bmi}</strong>{" "}
                            <Badge bg={
                              bmi < 18.5 ? "secondary" :
                              bmi < 23 ? "success" :
                              bmi < 25 ? "warning" : "danger"
                            }>
                              {label}
                            </Badge>
                          </>
                        ) : "-"}
                      </td>
                      <td>{h.bloodPressure || "-"}</td>
                      <td>{h.notes || "-"}</td>
                      <td className="text-end">
                        <Button size="sm" variant="outline-danger" onClick={() => del(h.healthId)}>
                          Xóa
                        </Button>
                      </td>
                    </tr>
                  );
                })
              ) : (
                <tr>
                  <td colSpan={8} className="text-center text-muted py-4">
                    Chưa có dữ liệu
                  </td>
                </tr>
              )}
            </tbody>
          </Table>
        </Card>
      )}
    </>
  );
};

export default HealthList;
