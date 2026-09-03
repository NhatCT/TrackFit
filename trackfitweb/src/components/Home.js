import { useEffect, useState, useMemo } from "react";
import { Container, Row, Col, Card, Button, Image } from "react-bootstrap";
import { authApis, endpoints } from "../configs/Apis";
import cookie from "react-cookies";
import TodayWorkout from "./TodayWorkout";
import ActivityRings from "./ActivityRings";
import MetricCard from "./MetricCard";
import HealthTrendChart from "./HealthTrendChart";
import { useTodaySummary, badgeFor } from "../utils/useTodaySummary";

import heroBg from "../img/hero-bg.jpg";
import servicePic from "../img/services/service-pic.jpg";
import sIcon1 from "../img/services/service-icon-1.png";
import sIcon2 from "../img/services/service-icon-2.png";
import sIcon3 from "../img/services/service-icon-3.png";
import sIcon4 from "../img/services/service-icon-4.png";

import c1 from "../img/classes/classes-1.jpg";
import c2 from "../img/classes/classes-2.jpg";
import c3 from "../img/classes/classes-3.jpg";
import c4 from "../img/classes/classes-4.jpg";
import c5 from "../img/classes/classes-5.jpg";
import c6 from "../img/classes/classes-6.jpg";
import c7 from "../img/classes/classes-7.jpg";
import c8 from "../img/classes/classes-8.jpg";

/* helper tạo background giống set-bg của template */
const bg = (url) => ({
  backgroundImage: `url('${url}')`,
  backgroundSize: "cover",
  backgroundPosition: "center",
});

/* map dữ liệu bài tập từ BE -> item hiển thị */
const toClassItem = (ex, fallbackImg) => ({
  title: ex?.name || "Exercise",
  coach: ex?.muscleGroup || ex?.targetGoal || "Bài tập",
  img: fallbackImg,
});

/* nhãn ngày kiểu "Thứ Tư, 20 tháng 8" */
const DOW_FULL = ["Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy"];
const todayLabel = () => {
  const d = new Date();
  return `${DOW_FULL[d.getDay()]}, ${d.getDate()} tháng ${d.getMonth() + 1}`;
};

/* mở Gutim Coach (ChatWidget lắng nghe sự kiện này) */
const openCoach = (question) =>
  window.dispatchEvent(new CustomEvent("gutim-open-chat", { detail: { question } }));

/* Shimmer skeleton block */
const Skeleton = ({ height = 20, width = "100%", radius = 8, style: sx = {} }) => (
  <div
    style={{
      height,
      width,
      borderRadius: radius,
      background: "linear-gradient(90deg,var(--surface-3) 25%,var(--surface-2) 50%,var(--surface-3) 75%)",
      backgroundSize: "200% 100%",
      animation: "gutim-shimmer 1.4s ease-in-out infinite",
      ...sx,
    }}
  />
);

/* Skeleton version of MetricCard */
const MetricCardSkeleton = () => (
  <div className="g-card h-100" style={{ padding: "var(--spacing-md)" }}>
    <Skeleton height={14} width={60} style={{ marginBottom: 14 }} />
    <Skeleton height={42} width={80} style={{ marginBottom: 10 }} />
    <Skeleton height={12} width="70%" />
  </div>
);

const Home = () => {
  const [exercises, setExercises] = useState([]);
  const [latestHealth, setLatestHealth] = useState(null);
  const [healthHistory, setHealthHistory] = useState([]);
  const [recos, setRecos] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  const isLoggedIn = !!cookie.load("token");
  const summary = useTodaySummary(isLoggedIn);

  useEffect(() => {
    if (!isLoggedIn) { setIsLoading(false); return; }
    const load = async () => {
      setIsLoading(true);
      try {
        const exRes = await authApis().get(endpoints.exercises, { params: { page: 1, pageSize: 8 } });
        const items = exRes?.data?.items || exRes?.data || [];
        setExercises(items);

        const healthRes = await authApis().get(endpoints.health);
        const healthItems = Array.isArray(healthRes.data) ? healthRes.data : (healthRes.data?.items || []);
        setHealthHistory(healthItems);
        if (healthItems.length > 0) {
          const sorted = [...healthItems].sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));
          setLatestHealth(sorted[0]);
        }

        try {
          const recoRes = await authApis().get(endpoints.recommendations, { params: { size: 4 } });
          const recoItems = Array.isArray(recoRes.data) ? recoRes.data : (recoRes.data?.items || []);
          setRecos(recoItems.slice(0, 4));
        } catch {
          setRecos([]);
        }
      } catch (e) {
        console.error("Home fetch error:", e);
      } finally {
        setIsLoading(false);
      }
    };
    load();
  }, [isLoggedIn]);

  const bmiData = useMemo(() => {
    if (!latestHealth || !latestHealth.height || !latestHealth.weight) return null;
    const h = Number(latestHealth.height);
    const w = Number(latestHealth.weight);
    if (!h || !w) return null;
    const bmi = +(w / Math.pow(h / 100, 2)).toFixed(1);
    let label = "Bình thường";
    let color = "success";
    if (bmi < 18.5) {
      label = "Thiếu cân";
      color = "info";
    } else if (bmi >= 23 && bmi < 25) {
      label = "Thừa cân";
      color = "warning";
    } else if (bmi >= 25) {
      label = "Béo phì";
      color = "danger";
    }
    return { bmi, label, color };
  }, [latestHealth]);

  // Xu hướng cân nặng: so bản ghi mới nhất với bản ghi cũ nhất
  const weightTrend = useMemo(() => {
    const withWeight = [...healthHistory]
      .filter((h) => Number(h.weight))
      .sort((a, b) => new Date(a.createdAt || 0) - new Date(b.createdAt || 0));
    if (withWeight.length < 2) return null;
    const first = Number(withWeight[0].weight);
    const last = Number(withWeight[withWeight.length - 1].weight);
    const diff = +(last - first).toFixed(1);
    return { diff, down: diff <= 0 };
  }, [healthHistory]);

  // ==== Vòng Activity (từ dữ liệu sẵn có) ====
  const nextMilestone = badgeFor(summary.streak).next || Math.max(summary.streak, 90);
  const rings = [
    {
      label: "Buổi tập / tuần",
      value: summary.weekSessions,
      max: summary.weekTarget,
      color: "var(--pink)",
      display: `${summary.weekSessions}/${summary.weekTarget}`,
    },
    {
      label: "Phút vận động hôm nay",
      value: summary.todayMinutes,
      max: summary.minutesTarget,
      color: "var(--green)",
      display: summary.todayMinutes,
      unit: "phút",
    },
    {
      label: "Chuỗi ngày",
      value: summary.streak,
      max: nextMilestone,
      color: "var(--cyan)",
      display: summary.streak,
      unit: "ngày",
    },
  ];
  const streakBadge = badgeFor(summary.streak);

  // Người dùng mới chưa có bất kỳ dữ liệu sức khỏe nào
  const isFirstVisit = !isLoading && !bmiData && healthHistory.length === 0;

  // BMI sub-text theo ngưỡng thực tế của người dùng
  const bmiSubText = bmiData
    ? (() => {
        const { bmi } = bmiData;
        if (bmi < 18.5) return "Bổ sung dinh dưỡng để tăng cơ";
        if (bmi < 23)   return "Bạn đang trong ngưỡng lý tưởng ✓";
        if (bmi < 25)   return "Mục tiêu: về dưới 23 BMI";
        return "Bắt đầu kế hoạch giảm mỡ";
      })()
    : null;

  // Fallback khi chưa login
  const fallbackClasses = [
    { title: "Yoga", coach: "Dẻo dai", img: c1 },
    { title: "Running", coach: "Cardio", img: c2 },
    { title: "Personal Training", coach: "Tổng hợp", img: c3 },
    { title: "Karate", coach: "Võ thuật", img: c4 },
    { title: "Dance", coach: "Nhảy", img: c5 },
    { title: "Weight Loss", coach: "Giảm cân", img: c6 },
  ];
  const classImgs = [c1, c2, c3, c4, c5, c6, c7, c8];
  const classData = exercises?.length
    ? exercises.map((ex, i) => toClassItem(ex, classImgs[i % classImgs.length]))
    : fallbackClasses;

  const services = [
    {
      icon: sIcon1,
      title: "Kế hoạch cá nhân",
      desc: "Lập kế hoạch tập luyện chi tiết 7 ngày/tuần, tùy chỉnh theo mục tiêu và thể trạng.",
    },
    {
      icon: sIcon2,
      title: "Gợi ý thông minh",
      desc: "Hệ thống phân tích hồ sơ sức khỏe và lịch sử tập luyện để đề xuất bài tập tối ưu.",
    },
    {
      icon: sIcon3,
      title: "Theo dõi sức khỏe",
      desc: "Ghi nhận chiều cao, cân nặng, BMI và huyết áp — theo dõi xu hướng theo thời gian.",
    },
    {
      icon: sIcon4,
      title: "Trợ lý sức khỏe",
      desc: "Trò chuyện và nhận tư vấn về dinh dưỡng, kỹ thuật bài tập và chế độ sinh hoạt.",
    },
  ];

  return (
    <>
      {isLoggedIn ? (
        /* ===== DASHBOARD "TÓM TẮT" (Apple Health) ===== */
        <section className="py-4">
          <Container>
            <h1 className="fw-bold g-round mb-1" style={{ letterSpacing: "-.02em", fontSize: "clamp(1.9rem,5vw,2.6rem)" }}>
              Tóm tắt
            </h1>
            <p className="mb-4" style={{ color: "var(--muted)", fontWeight: 500 }}>
              {todayLabel()}
              {summary.streak > 0 && <> · chuỗi {summary.streak} ngày liên tục 🔥</>}
            </p>

            {/* Keyframe shimmer — injected once per mount */}
            <style>{`@keyframes gutim-shimmer{0%{background-position:200% 0}100%{background-position:-200% 0}}`}</style>

            {/* Hàng 1: Vòng Activity + BMI + Chuỗi — skeleton khi đang tải */}
            {isLoading ? (
              <Row className="g-3">
                <Col lg={5} md={12}>
                  <div className="g-card h-100 d-flex align-items-center justify-content-center" style={{ minHeight: 160 }}>
                    <Skeleton height={132} width={132} radius={999} />
                  </div>
                </Col>
                <Col lg={4} md={6} xs={12} sm={6}><MetricCardSkeleton /></Col>
                <Col lg={3} md={6} xs={12} sm={6}><MetricCardSkeleton /></Col>
              </Row>
            ) : isFirstVisit ? (
              /* Onboarding nudge — user has no health data yet */
              <div
                className="g-card p-4 mb-1 text-center"
                style={{ border: "2px dashed color-mix(in srgb,var(--brand) 35%,transparent)" }}
              >
                <div style={{ fontSize: 42, marginBottom: 10 }} aria-hidden="true">📊</div>
                <h4 className="g-round fw-bold mb-2">Chào mừng đến với Gutim!</h4>
                <p style={{ color: "var(--muted)", maxWidth: 360, margin: "0 auto 20px", lineHeight: 1.6 }}>
                  Nhập chỉ số cơ thể đầu tiên để xem BMI, biểu đồ sức khỏe và nhận gợi ý bài
                  tập cá nhân hóa theo thể trạng của bạn.
                </p>
                <a
                  href="/health"
                  className="btn btn-primary fw-semibold px-4"
                  style={{ borderRadius: "var(--radius-sm)" }}
                >
                  Nhập chỉ số ngay →
                </a>
                <div style={{ marginTop: 10, fontSize: 12.5, color: "var(--muted)" }}>Chỉ mất 30 giây</div>
              </div>
            ) : (
              <Row className="g-3">
                <Col lg={5} md={12}>
                  <div className="g-card h-100 d-flex align-items-center justify-content-center">
                    <ActivityRings rings={rings} />
                  </div>
                </Col>
                <Col lg={4} md={6} xs={12} sm={6}>
                  <MetricCard
                    color="purple"
                    name="BMI"
                    icon={<span style={{ fontWeight: 900 }}>⚖</span>}
                    value={bmiData ? bmiData.bmi : "—"}
                    sub={
                      bmiData ? (
                        <>
                          <b style={{ color: "var(--ink)" }}>{bmiData.label}</b> · {bmiSubText}
                        </>
                      ) : (
                        <a href="/health" style={{ color: "var(--brand)" }}>Nhập chỉ số cơ thể →</a>
                      )
                    }
                  />
                </Col>
                <Col lg={3} md={6} xs={12} sm={6}>
                  <MetricCard
                    color="pink"
                    name="Chuỗi"
                    icon={<span>{streakBadge.emoji}</span>}
                    value={summary.streak}
                    unit="ngày"
                    sub={<>Danh hiệu: {streakBadge.label}</>}
                  />
                </Col>
              </Row>
            )}

            {/* Hàng 2 "Cơ thể": Cân nặng + biểu đồ & Kế hoạch hôm nay */}
            <div className="mt-4 mb-2 fw-bold" style={{ color: "var(--muted)", fontSize: 13 }}>Cơ thể</div>
            <Row className="g-3">
              <Col lg={7}>
                <div className="g-card h-100">
                  <div className="d-flex align-items-center gap-2 mb-2">
                    <span className="g-ic g-ic--green">
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2">
                        <path d="M3 17l6-6 4 4 8-8" />
                      </svg>
                    </span>
                    <span style={{ fontSize: 15, fontWeight: 700, color: "var(--green)" }}>Cân nặng</span>
                    {weightTrend && (
                      <span className={`g-trend ${weightTrend.down ? "g-trend--down" : "g-trend--up"} ms-auto`}>
                        {weightTrend.down ? "▼" : "▲"} {Math.abs(weightTrend.diff)} kg
                      </span>
                    )}
                  </div>
                  <div>
                    <span className="g-value g-num">
                      {latestHealth?.weight ?? "—"}
                      {latestHealth?.weight ? <span className="g-unit">kg</span> : null}
                    </span>
                  </div>
                  <div className="mt-3">
                    <HealthTrendChart records={healthHistory} height={200} />
                  </div>
                </div>
              </Col>
              <Col lg={5}>
                <TodayWorkout />
              </Col>
            </Row>

            {/* Hàng 3: Gutim Coach */}
            <div className="mt-4 mb-2 fw-bold" style={{ color: "var(--muted)", fontSize: 13 }}>Trợ lý & gợi ý</div>
            <div
              className="p-4"
              style={{
                borderRadius: "var(--radius)",
                background: "linear-gradient(135deg, var(--purple), var(--blue))",
                color: "#fff",
                boxShadow: "var(--shadow)",
              }}
            >
              <div style={{ fontSize: 12, fontWeight: 700, letterSpacing: ".04em", opacity: 0.9, textTransform: "uppercase" }}>
                ✦ Gutim Coach
              </div>
              <p style={{ fontSize: "1.02rem", lineHeight: 1.5, margin: "13px 0 18px", fontWeight: 500 }}>
                Hỏi mình về dinh dưỡng, kỹ thuật bài tập hay điều chỉnh mục tiêu — mình sẽ tư vấn dựa trên
                hồ sơ sức khỏe của bạn.
              </p>
              <div className="d-flex gap-2 flex-wrap">
                {["Ăn gì sau tập?", "Kiểm tra tư thế", "Điều chỉnh mục tiêu"].map((q) => (
                  <button
                    key={q}
                    onClick={() => openCoach(q)}
                    style={{
                      background: "rgba(255,255,255,.18)",
                      border: "1px solid rgba(255,255,255,.28)",
                      color: "#fff",
                      fontSize: 12.5,
                      fontWeight: 600,
                      borderRadius: "var(--radius-pill)",
                      padding: "7px 13px",
                      cursor: "pointer",
                    }}
                  >
                    {q}
                  </button>
                ))}
              </div>
            </div>

            {/* Hàng 4: Gợi ý cho bạn */}
            {recos.length > 0 && (
              <>
                <div className="mt-4 mb-2 d-flex align-items-center justify-content-between">
                  <span className="fw-bold" style={{ color: "var(--muted)", fontSize: 13 }}>Gợi ý cho bạn</span>
                  <Button size="sm" variant="outline-primary" href="/recommendations">Xem tất cả</Button>
                </div>
                <Row className="g-3">
                  {recos.map((r, idx) => (
                    <Col key={idx} md={3} xs={6}>
                      <div className="g-card g-card--tap h-100" onClick={() => (window.location.href = "/recommendations")}>
                        <div className="d-flex justify-content-between align-items-start mb-2">
                          <span className="g-ic g-ic--brand">▶</span>
                          {typeof r.score === "number" && (
                            <span className="g-round" style={{ fontWeight: 800, fontSize: 12, color: "var(--green)" }}>
                              {Math.round(r.score * 100)}%
                            </span>
                          )}
                        </div>
                        <h4 className="g-round" style={{ fontWeight: 800, fontSize: ".95rem", margin: "0 0 6px" }}>
                          {r.name}
                        </h4>
                        <div className="d-flex gap-1 flex-wrap">
                          {r.muscleGroup && <span className="g-pill">{r.muscleGroup}</span>}
                          {r.estimatedMinutes && <span className="g-pill">{r.estimatedMinutes} phút</span>}
                        </div>
                      </div>
                    </Col>
                  ))}
                </Row>
              </>
            )}
          </Container>
        </section>
      ) : (
        /* ===== HERO (chưa đăng nhập) ===== */
        <section style={{ ...bg(heroBg), position: "relative", color: "#fff", padding: "100px 0" }}>
          <div style={{ position: "absolute", inset: 0, background: "rgba(0,0,0,.45)" }} />
          <Container style={{ position: "relative", zIndex: 1 }}>
            <Row className="align-items-center">
              <Col lg={12}>
                <div className="mb-2 text-uppercase fw-semibold" style={{ letterSpacing: "2px", opacity: .9 }}>
                  GUTIM — SỨC KHỎE & TẬP LUYỆN
                </div>
                <h1 className="fw-bold" style={{ fontSize: "clamp(1.8rem, 4vw, 3rem)" }}>
                  Theo dõi sức khỏe,<br />tập luyện thông minh mỗi ngày
                </h1>
                <p className="lead mb-4" style={{ opacity: .9, maxWidth: 560 }}>
                  Gutim giúp bạn xây dựng kế hoạch tập luyện cá nhân hóa, nhận gợi ý bài tập phù hợp
                  và theo dõi hành trình sức khỏe mỗi ngày.
                </p>
                <div className="d-flex flex-wrap gap-2">
                  <Button href="/register" variant="light" className="fw-semibold px-4">
                    Đăng ký miễn phí
                  </Button>
                  <Button href="/exercises" variant="outline-light" className="fw-semibold px-4">
                    Khám phá bài tập
                  </Button>
                </div>
              </Col>
            </Row>
          </Container>
        </section>
      )}

      {/* ===== SERVICES (chỉ hiển thị khi chưa đăng nhập) ===== */}
      {!isLoggedIn && (
        <section data-aos="fade-up">
          <Container fluid>
            <Row className="g-0">
              <Col lg={6}>
                <div style={{ ...bg(servicePic), minHeight: 520 }} />
              </Col>
              <Col lg={6}>
                <div className="p-4 p-lg-5">
                  <h2 className="fw-bold mb-4">Tính năng nổi bật</h2>
                  <Row className="g-4">
                    {services.map((s, i) => (
                      <Col md={6} key={i}>
                        <Card className="h-100 shadow-sm border-0">
                          <Card.Body>
                            <Image src={s.icon} alt="" height={48} className="mb-3" />
                            <h5 className="fw-bold">{s.title}</h5>
                            <p className="text-muted m-0 small">{s.desc}</p>
                          </Card.Body>
                        </Card>
                      </Col>
                    ))}
                  </Row>
                </div>
              </Col>
            </Row>
          </Container>
        </section>
      )}

      {/* ===== EXERCISES (chỉ hiển thị khi chưa đăng nhập — tránh marketing noise trong dashboard) ===== */}
      {!isLoggedIn && <section className="py-5" data-aos="fade-up">
        <Container>
          <div className="d-flex align-items-center justify-content-between mb-3">
            <div>
              <h2 className="fw-bold m-0">Bài tập nổi bật</h2>
              <p className="text-muted m-0 small">Khám phá và thêm vào kế hoạch tập luyện của bạn</p>
            </div>
            <div className="d-flex gap-2">
              <Button size="sm" variant="outline-primary" href="/exercises">Tất cả bài tập</Button>
            </div>
          </div>

          <Row className="g-4">
            {classData.map((c, idx) => (
              <Col key={idx} lg={4} md={6}>
                <div className="position-relative rounded-3 overflow-hidden"
                     style={{ ...bg(c.img), height: 260, transition: "transform 0.3s" }}
                     onMouseEnter={(e) => e.currentTarget.style.transform = "scale(1.03)"}
                     onMouseLeave={(e) => e.currentTarget.style.transform = "scale(1)"}
                >
                  <div className="position-absolute bottom-0 start-0 w-100 p-3"
                       style={{ background: "linear-gradient(180deg,transparent,rgba(0,0,0,.65))", color: "#fff" }}>
                    <h5 className="mb-1">{c.title}</h5>
                    <div className="small" style={{ opacity: .9 }}>
                      {c.coach}
                    </div>
                  </div>
                </div>
              </Col>
            ))}
            {!classData.length && (
              <Col xs={12}><div className="text-center text-muted">Chưa có bài tập</div></Col>
            )}
          </Row>
        </Container>
      </section>}

      {/* ===== CTA Section (chỉ khi chưa đăng nhập) ===== */}
      {!isLoggedIn && (
        <section className="py-5" data-aos="fade-up">
          <Container>
            <Card
              className="border-0 text-center p-5"
              style={{
                background: "linear-gradient(135deg, var(--brand-soft), color-mix(in srgb, var(--blue) 12%, transparent))",
                borderRadius: "1.5rem",
              }}
            >
              <Card.Body>
                <h2 className="fw-bold mb-3">Bắt đầu hành trình sức khỏe của bạn</h2>
                <p className="text-muted mb-4" style={{ maxWidth: 520, margin: "0 auto" }}>
                  Đăng ký miễn phí để nhận kế hoạch tập luyện cá nhân hóa, gợi ý bài tập phù hợp
                  và theo dõi tiến trình sức khỏe hàng ngày.
                </p>
                <div className="d-flex justify-content-center gap-3">
                  <Button href="/register" variant="primary" className="fw-semibold px-4 py-2">
                    Đăng ký ngay
                  </Button>
                  <Button href="/login" variant="outline-primary" className="fw-semibold px-4 py-2">
                    Đăng nhập
                  </Button>
                </div>
              </Card.Body>
            </Card>
          </Container>
        </section>
      )}
    </>
  );
};

export default Home;
