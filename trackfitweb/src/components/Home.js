import { useEffect, useState } from "react";
import { Container, Row, Col, Card, Button, Image, Badge } from "react-bootstrap";
import { authApis, endpoints } from "../configs/Apis";
import cookie from "react-cookies";
import TodayWorkout from "./TodayWorkout";
import StreakWidget from "./StreakWidget";

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

const Home = () => {
  const [exercises, setExercises] = useState([]);
  const [stats, setStats] = useState(null);

  const isLoggedIn = !!cookie.load("token");

  useEffect(() => {
    const load = async () => {
      if (!isLoggedIn) return;
      try {
        const exRes = await authApis().get(endpoints.exercises, { params: { page: 1, pageSize: 8 } });
        const items = exRes?.data?.items || exRes?.data || [];
        setExercises(items);

        const stRes = await authApis().get(endpoints.statsSummary);
        setStats(stRes?.data || null);
      } catch (e) {
        console.error("Home fetch error:", e);
      }
    };
    load();
  }, [isLoggedIn]);

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
      title: "Gợi ý AI thông minh",
      desc: "AI phân tích hồ sơ sức khỏe và lịch sử tập luyện để đề xuất bài tập tối ưu.",
    },
    {
      icon: sIcon3,
      title: "Theo dõi sức khỏe",
      desc: "Ghi nhận chiều cao, cân nặng, BMI và huyết áp — theo dõi xu hướng theo thời gian.",
    },
    {
      icon: sIcon4,
      title: "Trợ lý Chatbot",
      desc: "Trò chuyện với AI Coach về dinh dưỡng, kỹ thuật bài tập và chế độ sinh hoạt.",
    },
  ];

  return (
    <>
      {/* ===== HERO ===== */}
      <section style={{ ...bg(heroBg), position: "relative", color: "#fff", padding: "100px 0" }}>
        <div style={{ position: "absolute", inset: 0, background: "rgba(0,0,0,.45)" }} />
        <Container style={{ position: "relative", zIndex: 1 }}>
          <Row className="align-items-center">
            <Col lg={isLoggedIn ? 8 : 12}>
              <div className="mb-2 text-uppercase fw-semibold" style={{ letterSpacing: "2px", opacity: .9 }}>
                GUTIM — SỨC KHỎE & TẬP LUYỆN
              </div>
              <h1 className="fw-bold" style={{ fontSize: "clamp(1.8rem, 4vw, 3rem)" }}>
                Theo dõi sức khỏe,<br />tập luyện thông minh cùng AI
              </h1>
              <p className="lead mb-4" style={{ opacity: .9, maxWidth: 560 }}>
                Gutim giúp bạn xây dựng kế hoạch tập luyện cá nhân hóa, nhận gợi ý bài tập từ AI
                và theo dõi hành trình sức khỏe mỗi ngày.
              </p>
              <div className="d-flex flex-wrap gap-2">
                {isLoggedIn ? (
                  <>
                    <Button href="/plans" variant="light" className="fw-semibold px-4">
                      📋 Kế hoạch của tôi
                    </Button>
                    <Button href="/recommendations" variant="outline-light" className="fw-semibold px-4">
                      🤖 Gợi ý AI
                    </Button>
                  </>
                ) : (
                  <>
                    <Button href="/register" variant="light" className="fw-semibold px-4">
                      Đăng ký miễn phí
                    </Button>
                    <Button href="/exercises" variant="outline-light" className="fw-semibold px-4">
                      Khám phá bài tập
                    </Button>
                  </>
                )}
              </div>
            </Col>

            {/* Quick Stats khi đã đăng nhập */}
            {isLoggedIn && (
              <Col lg={4} className="mt-4 mt-lg-0">
                <Card className="border-0 shadow-sm" style={{ backdropFilter: "blur(12px)", background: "rgba(17,26,43,0.85)" }}>
                  <Card.Body>
                    <div className="d-flex justify-content-between align-items-center mb-3">
                      <h6 className="m-0">Tổng quan tập luyện</h6>
                      <Badge bg="primary">Live</Badge>
                    </div>
                    <Row>
                      <Col xs={6} className="mb-3">
                        <div className="text-muted small">Tổng phút</div>
                        <div className="fs-4 fw-bold">{stats?.totalMinutes ?? "--"}</div>
                      </Col>
                      <Col xs={6} className="mb-3">
                        <div className="text-muted small">Số buổi</div>
                        <div className="fs-4 fw-bold">{stats?.sessions ?? "--"}</div>
                      </Col>
                      <Col xs={12}>
                        <div className="text-muted small">Bài phổ biến</div>
                        <div className="fw-semibold">{stats?.topExerciseName ?? "—"}</div>
                      </Col>
                    </Row>
                    <hr style={{ borderColor: "rgba(255,255,255,0.1)" }} />
                    <StreakWidget />
                    <div className="text-end mt-3">
                      <Button size="sm" variant="outline-primary" href="/stats/summary">Xem thống kê</Button>
                    </div>
                  </Card.Body>
                </Card>
              </Col>
            )}
          </Row>
        </Container>
      </section>

      {isLoggedIn && (
        <section className="py-4">
          <Container>
            <TodayWorkout />
          </Container>
        </section>
      )}

      {/* ===== SERVICES (tính năng nổi bật) ===== */}
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

      {/* ===== EXERCISES (map từ BE hoặc fallback) ===== */}
      <section className="py-5" data-aos="fade-up">
        <Container>
          <div className="d-flex align-items-center justify-content-between mb-3">
            <div>
              <h2 className="fw-bold m-0">Bài tập nổi bật</h2>
              <p className="text-muted m-0 small">Khám phá và thêm vào kế hoạch tập luyện của bạn</p>
            </div>
            <div className="d-flex gap-2">
              <Button size="sm" variant="outline-primary" href="/exercises">Tất cả bài tập</Button>
              {isLoggedIn && <Button size="sm" variant="primary" href="/plans">Lập kế hoạch</Button>}
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
      </section>

      {/* ===== CTA Section ===== */}
      {!isLoggedIn && (
        <section className="py-5" data-aos="fade-up">
          <Container>
            <Card
              className="border-0 text-center p-5"
              style={{
                background: "linear-gradient(135deg, rgba(255,107,53,0.12), rgba(76,201,240,0.12))",
                borderRadius: "1.5rem",
              }}
            >
              <Card.Body>
                <h2 className="fw-bold mb-3">Bắt đầu hành trình sức khỏe của bạn</h2>
                <p className="text-muted mb-4" style={{ maxWidth: 520, margin: "0 auto" }}>
                  Đăng ký miễn phí để nhận kế hoạch tập luyện cá nhân hóa, gợi ý bài tập từ AI
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

      {/* ===== Quick Links cho logged-in users ===== */}
      {isLoggedIn && (
        <section className="py-5" data-aos="fade-up">
          <Container>
            <Row className="g-4">
              <Col md={4}>
                <Card className="border-0 shadow-sm h-100 text-center p-4">
                  <Card.Body>
                    <div style={{ fontSize: "2.5rem" }} className="mb-3">📊</div>
                    <h5 className="fw-bold">Thống kê chi tiết</h5>
                    <p className="text-muted small">Biểu đồ phút tập, số buổi và tỷ lệ bài tập trong 30 ngày</p>
                    <Button variant="outline-primary" size="sm" href="/stats/summary">Xem thống kê</Button>
                  </Card.Body>
                </Card>
              </Col>
              <Col md={4}>
                <Card className="border-0 shadow-sm h-100 text-center p-4">
                  <Card.Body>
                    <div style={{ fontSize: "2.5rem" }} className="mb-3">💚</div>
                    <h5 className="fw-bold">Sức khỏe</h5>
                    <p className="text-muted small">Theo dõi cân nặng, BMI, huyết áp và xu hướng biến đổi</p>
                    <Button variant="outline-primary" size="sm" href="/health">Cập nhật sức khỏe</Button>
                  </Card.Body>
                </Card>
              </Col>
              <Col md={4}>
                <Card className="border-0 shadow-sm h-100 text-center p-4">
                  <Card.Body>
                    <div style={{ fontSize: "2.5rem" }} className="mb-3">🎯</div>
                    <h5 className="fw-bold">Mục tiêu</h5>
                    <p className="text-muted small">Thiết lập mục tiêu giảm cân, tăng cơ hoặc tăng sức bền</p>
                    <Button variant="outline-primary" size="sm" href="/goals">Quản lý mục tiêu</Button>
                  </Card.Body>
                </Card>
              </Col>
            </Row>
          </Container>
        </section>
      )}
    </>
  );
};

export default Home;
