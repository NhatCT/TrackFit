import { useEffect } from "react";
import { Container, Row, Col } from "react-bootstrap";
import AOS from "aos";
import "aos/dist/aos.css";
import logo from "../../img/logo.png";

const Footer = () => {
  useEffect(() => {
    AOS.init({ duration: 800, once: true });
  }, []);

  return (
    <footer className="p-4" style={{ background: "linear-gradient(to right, #0e3a57, #add8e6)", color: "white" }}>
      <Container>
        <Row className="mb-4 align-items-center" data-aos="fade-up">
          <Col md={4} className="d-flex align-items-center gap-2">
            <img src={logo} alt="TrackFit" style={{ height: 36 }} />
            <div>
              <h5 className="mb-1">TrackFit</h5>
              <p className="mb-0">© {new Date().getFullYear()} - Nền tảng Fitness &amp; Wellness.</p>
            </div>
          </Col>

          <Col md={4}>
            <h5>Liên Kết</h5>
            <ul className="list-unstyled m-0">
              <li><a href="/exercises" style={{ color: "#ff6b35", textDecoration: "none" }}>Bài Tập</a></li>
              <li><a href="/plans" style={{ color: "#ff6b35", textDecoration: "none" }}>Kế Hoạch</a></li>
              <li><a href="/stats/summary" style={{ color: "#ff6b35", textDecoration: "none" }}>Thống Kê</a></li>
            </ul>
          </Col>

          <Col md={4}>
            <h5>Liên Hệ</h5>
            <p className="mb-1">Email: support@trackfit.com</p>
            <p className="mb-0">Phone: +84 123 456 789</p>
          </Col>
        </Row>

        <hr style={{ borderColor: "#ffffff66" }} />

        <p className="text-center mb-0" data-aos="fade-up">
          © {new Date().getFullYear()} TrackFit. All Rights Reserved.
        </p>
      </Container>
    </footer>
  );
};

export default Footer;
