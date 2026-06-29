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
    <footer
      className="py-5"
      style={{
        background: "linear-gradient(180deg, #0b1220 0%, #0f1a2e 50%, #111a2b 100%)",
        color: "#e6edf3",
        borderTop: "1px solid rgba(255,255,255,0.06)",
      }}
    >
      <Container>
        <Row className="g-4 mb-4" data-aos="fade-up">
          {/* Brand Column */}
          <Col md={4}>
            <div className="d-flex align-items-center gap-2 mb-3">
              <img src={logo} alt="Gutim" style={{ height: 36 }} />
              <h5 className="mb-0 fw-bold text-white">GUTIM</h5>
            </div>
            <p className="text-muted small mb-3" style={{ maxWidth: 280 }}>
              Nền tảng theo dõi sức khỏe & tập luyện thông minh. Giúp bạn xây dựng thói quen lành mạnh mỗi ngày.
            </p>
            <div className="d-flex gap-3">
              <a href="https://facebook.com" target="_blank" rel="noreferrer" className="text-muted" style={{ fontSize: "1.2rem", transition: "color 0.2s" }}
                onMouseEnter={(e) => e.target.style.color = "#ff6b35"}
                onMouseLeave={(e) => e.target.style.color = ""}
              >
                <i className="bi bi-facebook"></i>
                <span role="img" aria-label="Facebook">📘</span>
              </a>
              <a href="https://instagram.com" target="_blank" rel="noreferrer" className="text-muted" style={{ fontSize: "1.2rem", transition: "color 0.2s" }}
                onMouseEnter={(e) => e.target.style.color = "#ff6b35"}
                onMouseLeave={(e) => e.target.style.color = ""}
              >
                <span role="img" aria-label="Instagram">📸</span>
              </a>
              <a href="https://youtube.com" target="_blank" rel="noreferrer" className="text-muted" style={{ fontSize: "1.2rem", transition: "color 0.2s" }}
                onMouseEnter={(e) => e.target.style.color = "#ff6b35"}
                onMouseLeave={(e) => e.target.style.color = ""}
              >
                <span role="img" aria-label="YouTube">▶️</span>
              </a>
            </div>
          </Col>

          {/* Quick Links */}
          <Col md={4}>
            <h6 className="text-uppercase text-muted small fw-bold mb-3" style={{ letterSpacing: "1px" }}>
              Liên Kết Nhanh
            </h6>
            <ul className="list-unstyled m-0">
              <li className="mb-2">
                <a href="/exercises" className="text-decoration-none" style={{ color: "#9fb0c5", transition: "color 0.2s" }}
                  onMouseEnter={(e) => e.target.style.color = "#ff6b35"}
                  onMouseLeave={(e) => e.target.style.color = "#9fb0c5"}>
                  Bài Tập
                </a>
              </li>
              <li className="mb-2">
                <a href="/plans" className="text-decoration-none" style={{ color: "#9fb0c5", transition: "color 0.2s" }}
                  onMouseEnter={(e) => e.target.style.color = "#ff6b35"}
                  onMouseLeave={(e) => e.target.style.color = "#9fb0c5"}>
                  Kế Hoạch
                </a>
              </li>
              <li className="mb-2">
                <a href="/stats/summary" className="text-decoration-none" style={{ color: "#9fb0c5", transition: "color 0.2s" }}
                  onMouseEnter={(e) => e.target.style.color = "#ff6b35"}
                  onMouseLeave={(e) => e.target.style.color = "#9fb0c5"}>
                  Thống Kê
                </a>
              </li>
              <li>
                <a href="/health" className="text-decoration-none" style={{ color: "#9fb0c5", transition: "color 0.2s" }}
                  onMouseEnter={(e) => e.target.style.color = "#ff6b35"}
                  onMouseLeave={(e) => e.target.style.color = "#9fb0c5"}>
                  Sức Khỏe
                </a>
              </li>
            </ul>
          </Col>

          {/* Contact */}
          <Col md={4}>
            <h6 className="text-uppercase text-muted small fw-bold mb-3" style={{ letterSpacing: "1px" }}>
              Liên Hệ
            </h6>
            <div className="mb-2 d-flex align-items-center gap-2">
              <span style={{ opacity: 0.7 }}>✉️</span>
              <span className="small" style={{ color: "#9fb0c5" }}>support@gutim.vn</span>
            </div>
            <div className="mb-2 d-flex align-items-center gap-2">
              <span style={{ opacity: 0.7 }}>📞</span>
              <span className="small" style={{ color: "#9fb0c5" }}>+84 382 766 336</span>
            </div>
            <div className="d-flex align-items-center gap-2">
              <span style={{ opacity: 0.7 }}>📍</span>
              <span className="small" style={{ color: "#9fb0c5" }}>TP. Hồ Chí Minh, Việt Nam</span>
            </div>
          </Col>
        </Row>

        <hr style={{ borderColor: "rgba(255,255,255,0.06)" }} />

        <div className="d-flex flex-column flex-md-row justify-content-between align-items-center gap-2" data-aos="fade-up">
          <p className="mb-0 text-muted small">
            © {new Date().getFullYear()} GUTIM. All Rights Reserved.
          </p>
          <p className="mb-0 text-muted small" style={{ opacity: 0.6 }}>
            Built with ❤️ for fitness enthusiasts
          </p>
        </div>
      </Container>
    </footer>
  );
};

export default Footer;
