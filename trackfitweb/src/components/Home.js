import { useEffect, useState } from "react";
import { Container, Row, Col, Card, Button, Image, Form, Carousel, Badge } from "react-bootstrap";
import { authApis, endpoints } from "../configs/Apis";
import cookie from "react-cookies";

import heroBg from "../img/hero-bg.jpg";
import aboutPic from "../img/about-pic.jpg";
import playPng from "../img/play.png";
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

import trainer1 from "../img/trainer/trainer-1.jpg";
import trainer2 from "../img/trainer/trainer-2.jpg";
import trainer3 from "../img/trainer/trainer-3.jpg";

import testi1 from "../img/testimonial/testimonial-1.jpg";
import quoteLeft from "../img/testimonial/quote-left.png";

import bannerBg from "../img/banner-bg.jpg";
import bannerPerson from "../img/banner-person.png";

import registerPic from "../img/register-pic.jpg";

import blog1 from "../img/blog/blog-1.jpg";
import blog2 from "../img/blog/blog-2.jpg";
import blog3 from "../img/blog/blog-3.jpg";

import fb1 from "../img/footer-banner/footer-banner-1.jpg";
import fb2 from "../img/footer-banner/footer-banner-2.jpg";
import footerSignup from "../img/footer-signup.jpg";

/* helper tạo background giống set-bg của template */
const bg = (url) => ({
  backgroundImage: `url('${url}')`,
  backgroundSize: "cover",
  backgroundPosition: "center",
});

/* map dữ liệu bài tập từ BE -> item hiển thị */
const toClassItem = (ex, fallbackImg) => ({
  title: ex?.name || "Exercise",
  coach: ex?.type || "Exercise",
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
    { title: "Yoga", coach: "Ryan Knight", img: c1 },
    { title: "Running", coach: "Randy Rivera", img: c2 },
    { title: "Personal Training", coach: "Cole Robertson", img: c3 },
    { title: "Karate", coach: "Kevin McCormick", img: c4 },
    { title: "Dance", coach: "Russell Lane", img: c5 },
    { title: "Weight Loss", coach: "Ryan Scott", img: c6 },
  ];
  const classImgs = [c1, c2, c3, c4, c5, c6, c7, c8];
  const classData = exercises?.length
    ? exercises.map((ex, i) => toClassItem(ex, classImgs[i % classImgs.length]))
    : fallbackClasses;

  const trainers = [
    { name: "Patrick Cortez", role: "Leader", img: trainer1 },
    { name: "Gregory Powers", role: "Gym coach", img: trainer2 },
    { name: "Walter Wagner", role: "Dance trainer", img: trainer3 },
  ];

  const blogs = [
    { title: "10 States At Risk of Rural Hospital Closures", date: "February 17, 2019", tag: "#Gym", img: blog1 },
    { title: "Diver who helped save Thai soccer team", date: "February 17, 2019", tag: "#Sport", img: blog2 },
    { title: "Man gets life in prison for stabbing", date: "February 17, 2019", tag: "#Body", img: blog3 },
  ];

  const membership = [
    { name: "Basic", price: 17, unit: "/01 mo", features: [["Duration","12 months"],["Personal trainer","00 person"],["Amount of people","01 person"],["Number of visits","Unlimited"]] },
    { name: "Standard", price: 57, unit: "/01 mo", features: [["Duration","12 months"],["Personal trainer","01 person"],["Amount of people","01 person"],["Number of visits","Unlimited"]] },
    { name: "Premium", price: 98, unit: "/01 mo", features: [["Duration","12 months"],["Personal trainer","01 person"],["Amount of people","01 person"],["Number of visits","Unlimited"]] },
  ];

  return (
    <>
      {/* ===== HERO ===== */}
      <section style={{ ...bg(heroBg), position: "relative", color: "#fff", padding: "100px 0" }}>
        <div style={{ position: "absolute", inset: 0, background: "rgba(0,0,0,.35)" }} />
        <Container style={{ position: "relative", zIndex: 1 }}>
          <Row>
            <Col lg={8}>
              <div className="mb-2 text-uppercase fw-semibold" style={{ letterSpacing: "2px", opacity: .9 }}>
                FITNESS ELEMENTS
              </div>
              <h1 className="fw-bold">BMI CALCULATOR</h1>
              <p className="lead mb-4">
                Gutim comes packed with the user-friendly BMI Calculator<br /> shortcode which lets
              </p>
              <div className="d-flex flex-wrap gap-2">
                <Button href="/plans" variant="light">Bắt đầu kế hoạch</Button>
                <Button href="/exercises" variant="outline-light">Khám phá bài tập</Button>
              </div>
            </Col>

            {/* quick stats nếu đã đăng nhập */}
            {isLoggedIn && (
              <Col lg={4} className="mt-4 mt-lg-0">
                <Card className="border-0 shadow-sm">
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

      {/* ===== ABOUT ===== */}
      <section className="py-5">
        <Container>
          <Row className="align-items-center g-4">
            <Col lg={6}>
              <div className="position-relative">
                <Image src={aboutPic} alt="About" fluid style={{ borderRadius: "1rem" }} />
                <a
                  href="https://www.youtube.com/watch?v=SlPhMPnQ58k"
                  className="position-absolute top-50 start-50 translate-middle"
                  title="Play"
                >
                  <Image src={playPng} alt="Play" width={80} height={80} />
                </a>
              </div>
            </Col>
            <Col lg={6}>
              <h2 className="fw-bold">Story About Us</h2>
              <p className="text-muted">
                Lorem ipsum proin gravida nibh vel velit auctor aliquet. Aenean pretium sollicitudin, nascetur auci elit…
              </p>
              <p className="text-muted">
                Etiam rhoncus. Maecenas tempus, tellus eget condimentum rhoncus, gravida quam semper libero…
              </p>
              <Button variant="primary">Read More</Button>
            </Col>
          </Row>
        </Container>
      </section>

      {/* ===== SERVICES ===== */}
      <section>
        <Container fluid>
          <Row className="g-0">
            <Col lg={6}>
              <div style={{ ...bg(servicePic), minHeight: 520 }} />
            </Col>
            <Col lg={6}>
              <div className="p-4 p-lg-5">
                <Row className="g-4">
                  <Col md={6}>
                    <Card className="h-100 shadow-sm border-0 bg-light">
                      <Card.Body>
                        <Image src={sIcon1} alt="" height={48} className="mb-3" />
                        <h4>Strategies</h4>
                        <p className="text-muted m-0">Aenean massa. Cum sociis Theme et natoque penatibus…</p>
                      </Card.Body>
                    </Card>
                    <Card className="h-100 shadow-sm border-0 bg-light mt-4">
                      <Card.Body>
                        <Image src={sIcon3} alt="" height={48} className="mb-3" />
                        <h4>Workout</h4>
                        <p className="text-muted m-0">Aenean massa. Cum sociis Theme et natoque penatibus…</p>
                      </Card.Body>
                    </Card>
                  </Col>
                  <Col md={6}>
                    <Card className="h-100 shadow-sm border-0">
                      <Card.Body>
                        <Image src={sIcon2} alt="" height={48} className="mb-3" />
                        <h4>Yoga</h4>
                        <p className="text-muted m-0">Aenean massa. Cum sociis Theme et natoque penatibus…</p>
                      </Card.Body>
                    </Card>
                    <Card className="h-100 shadow-sm border-0 mt-4">
                      <Card.Body>
                        <Image src={sIcon4} alt="" height={48} className="mb-3" />
                        <h4>Weight Loss</h4>
                        <p className="text-muted m-0">Aenean massa. Cum sociis Theme et natoque penatibus…</p>
                      </Card.Body>
                    </Card>
                  </Col>
                </Row>
              </div>
            </Col>
          </Row>
        </Container>
      </section>

      {/* ===== CLASSES (map từ BE hoặc fallback) ===== */}
      <section className="py-5">
        <Container>
          <div className="d-flex align-items-center justify-content-between mb-3">
            <h2 className="fw-bold m-0">UNLIMITED CLASSES</h2>
            <div className="d-flex gap-2">
              <Button size="sm" variant="outline-primary" href="/exercises">Tất cả bài tập</Button>
              {isLoggedIn && <Button size="sm" variant="primary" href="/plans">Lập kế hoạch</Button>}
            </div>
          </div>

          <Row className="g-4">
            {classData.map((c, idx) => (
              <Col key={idx} lg={4} md={6}>
                <div className="position-relative rounded-3 overflow-hidden"
                     style={{ ...bg(c.img), height: 260 }}>
                  <div className="position-absolute bottom-0 start-0 w-100 p-3"
                       style={{ background: "linear-gradient(180deg,transparent,rgba(0,0,0,.6))", color: "#fff" }}>
                    <h5 className="mb-1">{c.title}</h5>
                    <div className="small" style={{ opacity: .9 }}>
                      <i className="fa fa-user" /> {c.coach}
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

      {/* ===== TRAINERS ===== */}
      <section className="py-5">
        <Container>
          <div className="text-center mb-4">
            <h2 className="fw-bold">EXPERT TRAINERS</h2>
          </div>
          <Row className="g-4">
            {trainers.map((t, i) => (
              <Col key={i} lg={4} md={6}>
                <Card className="border-0 shadow-sm h-100">
                  <Image src={t.img} alt={t.name} fluid />
                  <Card.Body>
                    <h5 className="mb-0">{t.name}</h5>
                    <div className="text-muted small mb-2">{t.role}</div>
                    <p className="text-muted">
                      non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem.
                    </p>
                    <div className="d-flex gap-2">
                      <a href="#fb" aria-label="fb"><i className="fa fa-facebook" /></a>
                      <a href="#ig" aria-label="ig"><i className="fa fa-instagram" /></a>
                      <a href="#tw" aria-label="tw"><i className="fa fa-twitter" /></a>
                      <a href="#pt" aria-label="pt"><i className="fa fa-pinterest" /></a>
                    </div>
                  </Card.Body>
                </Card>
              </Col>
            ))}
          </Row>
        </Container>
      </section>

      {/* ===== TESTIMONIAL (Carousel) ===== */}
      <section className="py-5">
        <Container>
          <div className="text-center mb-4">
            <h2 className="fw-bold text-capitalize">success stories</h2>
          </div>
          <Row className="justify-content-center">
            <Col lg={10}>
              <Carousel variant="dark" interval={4000}>
                {[1, 2].map((n) => (
                  <Carousel.Item key={n}>
                    <Row className="align-items-center g-4">
                      <Col md={8}>
                        <p className="lead">
                          Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et
                          dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea
                          commodo consequat.
                        </p>
                        <h5 className="mb-0">Patrick Cortez</h5>
                        <div className="text-muted">Leader</div>
                      </Col>
                      <Col md={4}>
                        <div className="position-relative">
                          <Image src={testi1} alt="" fluid className="rounded-3" />
                          <Image
                            src={quoteLeft}
                            alt=""
                            width={48}
                            className="position-absolute top-0 start-0 translate-middle"
                            style={{ top: 16, left: 16 }}
                          />
                        </div>
                      </Col>
                    </Row>
                  </Carousel.Item>
                ))}
              </Carousel>
            </Col>
          </Row>
        </Container>
      </section>

      {/* ===== BANNER ===== */}
      <section style={{ ...bg(bannerBg) }}>
        <Container className="py-5">
          <Row className="align-items-center">
            <Col lg={6}>
              <div className="text-white" style={{ textShadow: "0 2px 10px rgba(0,0,0,.5)" }}>
                <h2 className="fw-bold">Get training today</h2>
                <p>Gimply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry’s standard.</p>
                <Button variant="light" className="banner-btn">Contact Now</Button>
              </div>
            </Col>
            <Col lg={5} className="ms-auto d-none d-lg-block">
              <Image src={bannerPerson} alt="" fluid />
            </Col>
          </Row>
        </Container>
      </section>

      {/* ===== MEMBERSHIP ===== */}
      <section className="py-5">
        <Container>
          <div className="text-center mb-4">
            <h2 className="fw-bold">MEMBERSHIP PLANS</h2>
          </div>
          <Row className="g-4">
            {membership.map((m, i) => (
              <Col key={i} lg={4}>
                <Card className="h-100 shadow-sm border-0">
                  <div className="p-3 border-bottom">
                    <h4 className="m-0">{m.name}</h4>
                  </div>
                  <Card.Body>
                    <h2 className="fw-bold">
                      ${m.price}<span className="fs-6 text-muted">{m.unit}</span>
                    </h2>
                    <ul className="list-unstyled mt-3">
                      {m.features.map(([k,v], idx) => (
                        <li key={idx} className="d-flex justify-content-between border-bottom py-2">
                          <p className="m-0">{k}</p><span className="text-muted">{v}</span>
                        </li>
                      ))}
                    </ul>
                    <Button className="w-100 mt-2" variant="primary">Start Now</Button>
                  </Card.Body>
                </Card>
              </Col>
            ))}
          </Row>
        </Container>
      </section>

      {/* ===== REGISTER ===== */}
      <section className="py-5">
        <Container>
          <Row className="align-items-center g-4">
            <Col lg={8}>
              <div className="mb-3">
                <h2 className="fw-bold">Register Now</h2>
                <p className="text-muted">The First 7 Day Trial Is Completely Free With The Teacher</p>
              </div>
              <Form className="row g-3">
                <Col lg={6}>
                  <Form.Label>First Name</Form.Label>
                  <Form.Control placeholder="First Name" />
                </Col>
                <Col lg={6}>
                  <Form.Label>Your email address</Form.Label>
                  <Form.Control type="email" placeholder="Email" />
                </Col>
                <Col lg={6}>
                  <Form.Label>Last Name</Form.Label>
                  <Form.Control placeholder="Last Name" />
                </Col>
                <Col lg={6}>
                  <Form.Label>Mobile No*</Form.Label>
                  <Form.Control placeholder="Mobile" />
                </Col>
                <Col xs={12}>
                  <Button className="register-btn" href="/register">Get Started</Button>
                </Col>
              </Form>
            </Col>
            <Col lg={4}>
              <Image src={registerPic} alt="" fluid className="rounded-3 shadow-sm" />
            </Col>
          </Row>
        </Container>
      </section>

      {/* ===== LATEST BLOG ===== */}
      <section className="py-5">
        <Container>
          <div className="text-center mb-4">
            <h2 className="fw-bold">Latest Blog</h2>
          </div>
        </Container>
        <Container>
          <Row className="g-4">
            {[blogs[0], blogs[1], blogs[2]].map((b, i) => (
              <Col key={i} lg={4} md={6}>
                <Card className="border-0 shadow-sm h-100">
                  <Image src={b.img} alt={b.title} fluid />
                  <Card.Body>
                    <div className="d-flex justify-content-between align-items-center mb-2">
                      <div className="small text-muted">{b.date}</div>
                      <a href="#tag" className="small">{b.tag}</a>
                    </div>
                    <h5><a href="#blog" className="text-decoration-none text-dark">{b.title}</a></h5>
                  </Card.Body>
                </Card>
              </Col>
            ))}
          </Row>
        </Container>
      </section>

      {/* ===== FOOTER BANNER (2 cột) ===== */}
      <section className="py-4">
        <Container fluid>
          <Row className="g-4">
            <Col lg={6}>
              <div className="text-white rounded-3 p-4 p-lg-5" style={{ ...bg(fb1) }}>
                <span className="text-uppercase">New member</span>
                <h2 className="fw-bold">7 days for free</h2>
                <p>Complete the training sessions with us, surely you will be happy</p>
                <Button variant="light">Get Started</Button>
              </div>
            </Col>
            <Col lg={6}>
              <div className="text-white rounded-3 p-4 p-lg-5" style={{ ...bg(fb2) }}>
                <span className="text-uppercase">contact us</span>
                <h2 className="fw-bold">09 746 204</h2>
                <p>If you trust us on your journey they dark sex does not disappoint you!</p>
                <Button variant="light">Get Started</Button>
              </div>
            </Col>
          </Row>
        </Container>
      </section>

      {/* ===== SUBSCRIBE (footer-signup.jpg) ===== */}
      <section className="py-5">
        <Container>
          <div className="rounded-3 p-4 p-md-5 d-flex flex-column flex-md-row align-items-center justify-content-between"
               style={{ ...bg(footerSignup), color: "#fff" }}>
            <div className="mb-3 mb-md-0" style={{ textShadow: "0 2px 10px rgba(0,0,0,.4)" }}>
              <h4 className="fw-bold m-0">Subscribe To Our Mailing List</h4>
              <div>Sign up to receive the latest information</div>
            </div>
            <Form className="d-flex bg-white rounded-pill p-1" style={{ minWidth: 320 }}>
              <Form.Control placeholder="Enter Your Mail" className="border-0 rounded-pill" />
              <Button className="rounded-pill px-3" variant="primary">
                <i className="fa fa-send" />
              </Button>
            </Form>
          </div>
        </Container>
      </section>
    </>
  );
};

export default Home;
