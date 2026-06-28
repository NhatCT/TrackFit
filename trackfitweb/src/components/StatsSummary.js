// src/components/StatsSummary.jsx
import { useEffect, useMemo, useState } from "react";
import { Button, Card, Col, Form, Row, Badge, Alert } from "react-bootstrap";
import { authApis, endpoints } from "../configs/Apis";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  ArcElement,
  Tooltip,
  Legend,
} from "chart.js";
import { Bar, Doughnut } from "react-chartjs-2";
import AOS from "aos";
import "aos/dist/aos.css";

ChartJS.register(CategoryScale, LinearScale, BarElement, ArcElement, Tooltip, Legend);

const fmt = (d) => new Date(d).toISOString().slice(0, 10);
const todayStr = () => fmt(new Date());
const daysAgoStr = (n) => {
  const d = new Date();
  d.setDate(d.getDate() - n);
  return fmt(d);
};

// ===== cấu hình hiển thị =====
const MAX_X_TICKS = 15; // số nhãn tối đa trên trục X
const axisColor = "rgba(255,255,255,0.75)";
const gridColor = "rgba(255,255,255,0.08)";
const legendColor = "rgba(255,255,255,0.85)";

const StatsSummary = () => {
  const [from, setFrom] = useState(daysAgoStr(29)); // 30 ngày gần nhất
  const [to, setTo] = useState(todayStr());
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");

  useEffect(() => {
    AOS.init({ duration: 700, once: true });
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const load = async (e) => {
    e?.preventDefault?.();
    setLoading(true);
    setErr("");
    try {
      const r = await authApis().get(endpoints.statsSummary, {
        params: { from: from || undefined, to: to || undefined },
      });
      setData(r.data);
      // giúp AOS đo lại layout sau khi chart render
      setTimeout(() => {
        try {
          AOS.refreshHard();
        } catch {}
      }, 0);
    } catch (ex) {
      console.error(ex);
      setErr("Không tải được thống kê. Vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  };

  /** ======= Chuẩn hoá dữ liệu để vẽ biểu đồ ======= */
  const daily = useMemo(() => data?.dailyMinutes || [], [data]);
  const exSess = useMemo(() => data?.exerciseSessions || [], [data]);

  // Fallback nếu API chưa có mảng dailyMinutes
  const barLabels = daily.length
    ? daily.map((d) => d.date)
    : from && to
    ? [from + " → " + to]
    : ["Khoảng thời gian"];
  const barValues = daily.length ? daily.map((d) => d.minutes || 0) : [data?.totalMinutes || 0];

  const doughnutLabels = exSess.length ? exSess.map((x) => x.name) : [data?.topExerciseName || "—"];
  const doughnutValues = exSess.length ? exSess.map((x) => x.sessions || 0) : [data?.sessions || 0];

  const totalSessions = (exSess.length ? exSess : [{ sessions: data?.sessions || 0 }]).reduce(
    (s, x) => s + (x.sessions || 0),
    0
  );

  // ===== thưa nhãn & chiều rộng canvas =====
  const tickStep = Math.max(1, Math.ceil(barLabels.length / MAX_X_TICKS));
  const canvasMinWidth = Math.max(720, barLabels.length * 42); // ~42px/nhãn

  return (
    <>
      {/* Header đồng bộ */}
      <div className="d-flex justify-content-between align-items-center mb-3" data-aos="fade-right">
        <div className="d-flex align-items-center gap-2">
          <h3 className="m-0">Thống kê tổng quan</h3>
          {data?.sessions != null && <Badge bg="secondary">{data.sessions} buổi</Badge>}
        </div>
      </div>

      {/* Bộ lọc ngày */}
      <Card className="shadow-sm border-0 mb-3 hoverable" data-aos="fade-up">
        <Card.Body>
          <Form className="row g-2 align-items-end" onSubmit={load}>
            <div className="col-12 col-md-4">
              <Form.Label className="small text-uppercase text-muted">Từ ngày</Form.Label>
              <Form.Control type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
            </div>
            <div className="col-12 col-md-4">
              <Form.Label className="small text-uppercase text-muted">Đến ngày</Form.Label>
              <Form.Control type="date" value={to} onChange={(e) => setTo(e.target.value)} />
            </div>
            <div className="col-12 col-md-4 d-flex gap-2 justify-content-end">
              <Button type="submit" variant="outline-primary">Lấy dữ liệu</Button>
              <Button
                type="button"
                variant="outline-secondary"
                onClick={() => {
                  setFrom(daysAgoStr(29));
                  setTo(todayStr());
                  setTimeout(load, 0);
                }}
              >
                30 ngày gần nhất
              </Button>
            </div>
          </Form>
          {err && <Alert variant="danger" className="mt-3 mb-0">{err}</Alert>}
        </Card.Body>
      </Card>

      {/* KPI + Charts */}
      <Row className="g-3">
        {/* KPI Cards */}
        <Col md={4} data-aos="fade-up">
          <Card className="shadow-sm border-0 h-100">
            <Card.Body>
              <div className="text-muted small text-uppercase">Tổng thời lượng</div>
              <div className="display-6 fw-semibold">{data?.totalMinutes ?? "—"}</div>
              <div className="text-muted">phút</div>
              <hr />
              <div className="text-muted small text-uppercase">Số buổi</div>
              <div className="h4 m-0">{data?.sessions ?? "—"}</div>
              <hr />
              <div className="text-muted small text-uppercase">Bài tập phổ biến</div>
              <div className="h6 m-0">{data?.topExerciseName || "—"}</div>
            </Card.Body>
          </Card>
        </Col>

        {/* Bar chart */}
        <Col md={8} data-aos="fade-up">
          <Card className="shadow-sm border-0 h-100">
            <Card.Body>
              <div className="d-flex justify-content-between align-items-center mb-2">
                <div className="text-muted small text-uppercase">Phút luyện tập theo ngày</div>
              </div>
              {loading ? (
                <div className="text-center py-5">Đang tải...</div>
              ) : barValues.every((v) => !v) ? (
                <div className="text-muted text-center py-5">Không có dữ liệu để vẽ</div>
              ) : (
                // bọc scroll ngang để không vỡ layout khi nhiều ngày
                <div style={{ overflowX: "auto" }}>
                  <div style={{ minWidth: canvasMinWidth }}>
                    {/* KHUNG CỐ ĐỊNH CHIỀU CAO */}
                    <div style={{ height: 280, position: "relative" }}>
                      <Bar
                        data={{
                          labels: barLabels,
                          datasets: [
                            {
                              label: "Phút",
                              data: barValues,
                              borderWidth: 1,
                              backgroundColor: "rgba(54, 162, 235, 0.5)",
                              borderColor: "rgba(54, 162, 235, 1)",
                              maxBarThickness: 28,
                              categoryPercentage: 0.8,
                              barPercentage: 0.9,
                            },
                          ],
                        }}
                        options={{
                          responsive: true,
                          maintainAspectRatio: false, // quan trọng khi dùng khung cố định
                          plugins: { legend: { display: false } },
                          layout: { padding: { right: 8 } },
                          scales: {
                            x: {
                              grid: { color: gridColor, drawBorder: false },
                              ticks: {
                                color: axisColor,
                                maxRotation: 0,
                                minRotation: 0,
                                autoSkip: false, // tự xử lý skip nhãn
                                callback: (value, index) => {
                                  if (index % tickStep === 0) {
                                    const label = barLabels[index] ?? "";
                                    // rút gọn yyyy-mm-dd -> mm-dd
                                    return label.length >= 10 ? label.slice(5) : label;
                                  }
                                  return "";
                                },
                              },
                            },
                            y: {
                              beginAtZero: true,
                              ticks: { stepSize: 10, color: axisColor },
                              grid: { color: gridColor, drawBorder: false },
                            },
                          },
                        }}
                        // KHÔNG truyền height/width prop
                      />
                    </div>
                  </div>
                </div>
              )}
            </Card.Body>
          </Card>
        </Col>

        {/* Doughnut chart */}
        <Col md={6}>
          <Card className="shadow-sm border-0">
            <Card.Body>
              <div className="text-muted small text-uppercase mb-2">
                Tỷ lệ số buổi theo bài tập
              </div>

              {loading ? (
                <div className="text-center py-5">Đang tải...</div>
              ) : doughnutValues.every((v) => !v) ? (
                <div className="text-muted text-center py-5">Không có dữ liệu để vẽ</div>
              ) : (
                // KHUNG CỐ ĐỊNH + CHẶN PHÓNG TO
                <div
                  style={{
                    width: "100%",
                    maxWidth: 520,
                    height: 320,
                    position: "relative",
                    margin: "0 auto",
                    overflow: "hidden",
                  }}
                >
                  <Doughnut
                    data={{
                      labels: doughnutLabels,
                      datasets: [
                        {
                          data: doughnutValues,
                          backgroundColor: [
                            "rgba(75, 192, 192, 0.6)",
                            "rgba(255, 159, 64, 0.6)",
                            "rgba(153, 102, 255, 0.6)",
                            "rgba(255, 205, 86, 0.6)",
                            "rgba(201, 203, 207, 0.6)",
                            "rgba(54, 162, 235, 0.6)",
                          ],
                          borderWidth: 1,
                        },
                      ],
                    }}
                    options={{
                      responsive: true,
                      maintainAspectRatio: false, // bắt buộc khi khung cố định
                      resizeDelay: 150,
                      animation: { duration: 0 },
                      radius: "90%",
                      cutout: "60%",
                      layout: { padding: 8 },
                      plugins: {
                        legend: { position: "bottom", labels: { color: legendColor } },
                        tooltip: {
                          callbacks: {
                            label: (ctx) => {
                              const v = ctx.raw || 0;
                              const pct = totalSessions ? Math.round((v * 100) / totalSessions) : 0;
                              return `${ctx.label}: ${v} buổi (${pct}%)`;
                            },
                          },
                        },
                      },
                    }}
                    // KHÔNG truyền height/width prop
                  />
                </div>
              )}
            </Card.Body>
          </Card>
        </Col>

        {/* Bảng fallback / tham khảo nhanh */}
        <Col md={6} data-aos="fade-up">
          <Card className="shadow-sm border-0 h-100">
            <Card.Body>
              <div className="text-muted small text-uppercase mb-2">Tóm tắt</div>
              {!data ? (
                <div className="text-center py-5">Chưa có dữ liệu</div>
              ) : (
                <ul className="m-0">
                  <li>
                    <strong>Khoảng thời gian:</strong> {from} → {to}
                  </li>
                  <li>
                    <strong>Tổng thời lượng:</strong> {data.totalMinutes ?? "—"} phút
                  </li>
                  <li>
                    <strong>Số buổi:</strong> {data.sessions ?? "—"}
                  </li>
                  <li>
                    <strong>Bài tập phổ biến:</strong> {data.topExerciseName || "—"}
                  </li>
                </ul>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </>
  );
};

export default StatsSummary;
