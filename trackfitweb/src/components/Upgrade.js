import React, { useState, useContext, useEffect } from "react";
import { Container, Row, Col, Card, Button, Spinner, Alert } from "react-bootstrap";
import { MyUserContext } from "../configs/Context";
import { useNavigate } from "react-router-dom";
import AOS from "aos";
import "aos/dist/aos.css";

const Upgrade = () => {
  const [user, dispatch] = useContext(MyUserContext);
  const navigate = useNavigate();

  const [selectedPlan, setSelectedPlan] = useState(null); // 'monthly' | 'yearly'
  const [step, setStep] = useState(1); // 1: choose, 2: pay, 3: success
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    AOS.init({ duration: 800, once: true });
  }, []);

  if (!user) {
    return (
      <Container className="py-5 text-center">
        <Alert variant="warning">Vui lòng đăng nhập để nâng cấp tài khoản.</Alert>
      </Container>
    );
  }

  const plans = {
    monthly: {
      name: "Gói Tháng (Pro Monthly)",
      price: 99000,
      priceFormatted: "99.000đ",
      desc: "Lựa chọn linh hoạt, huỷ bất kỳ lúc nào.",
      duration: "tháng",
      badge: "Phổ biến",
    },
    yearly: {
      name: "Gói Năm (Pro Yearly)",
      price: 499000,
      priceFormatted: "499.000đ",
      desc: "Tiết kiệm 58% so với gói tháng. Tối ưu nhất!",
      duration: "năm",
      badge: "Tiết kiệm 58%",
      isBestValue: true,
    },
  };

  const handleSelectPlan = (planKey) => {
    setSelectedPlan(plans[planKey]);
    setStep(2);
  };

  const handleConfirmPayment = () => {
    setLoading(true);
    setError(null);

    // Simulate verification check (3 seconds)
    setTimeout(() => {
      setLoading(false);
      // Upgrade user in context
      dispatch({ type: "updateProfile", payload: { isPremium: true } });
      setStep(3);
    }, 3000);
  };

  if (user.isPremium && step !== 3) {
    return (
      <Container className="py-5 text-center" data-aos="fade-up">
        <div style={{ maxWidth: "600px", margin: "0 auto" }}>
          <div className="fs-1 mb-3">👑</div>
          <h2 className="text-white mb-3">Bạn đã sở hữu GUTIM PRO</h2>
          <p className="text-light-50 mb-4">
            Tài khoản của bạn hiện đang là hội viên <strong>Premium</strong>. Bạn có toàn quyền truy cập bản đồ không giới hạn, chat với Gutim Coach 24/7 và nhận các gợi ý bài tập thông minh.
          </p>
          <Button variant="warning" onClick={() => navigate("/")} className="px-4 py-2 fw-bold text-dark">
            Về Trang Chủ
          </Button>
        </div>
      </Container>
    );
  }

  return (
    <Container className="py-4 text-light" data-aos="fade-up">
      {/* Styles for Upgrade page */}
      <style>{`
        .upgrade-card {
          background: #111a2b;
          border: 1px solid #1f2d47;
          border-radius: 16px;
          transition: all 0.3s ease-in-out;
          position: relative;
          overflow: hidden;
        }
        .upgrade-card:hover {
          transform: translateY(-8px);
          border-color: #ff6b35;
          box-shadow: 0 10px 30px rgba(255, 107, 53, 0.15);
        }
        .upgrade-card.best-value {
          border: 2px solid #ff6b35;
          box-shadow: 0 5px 20px rgba(255, 107, 53, 0.1);
        }
        .plan-badge {
          position: absolute;
          top: 15px;
          right: -30px;
          background: #ff6b35;
          color: #fff;
          font-size: 0.75rem;
          font-weight: bold;
          padding: 4px 30px;
          transform: rotate(45deg);
        }
        .price-text {
          font-size: 2.5rem;
          color: #ff6b35;
          font-weight: 800;
        }
        .feature-item {
          display: flex;
          align-items: center;
          gap: 10px;
          font-size: 0.9rem;
          margin-bottom: 12px;
        }
        .qr-card {
          background: #111a2b;
          border: 1px solid #1f2d47;
          border-radius: 16px;
          padding: 24px;
        }
        .text-orange {
          color: #ff6b35;
        }
        .compare-table {
          background: #111a2b;
          border: 1px solid #1f2d47;
          border-radius: 16px;
          overflow: hidden;
        }
        .compare-table th,
        .compare-table td {
          padding: 12px 16px;
          border-color: rgba(255,255,255,0.06) !important;
          vertical-align: middle;
        }
        .compare-table thead th {
          background: #0f172a;
          color: #9fb0c5;
          font-size: 0.8rem;
          text-transform: uppercase;
          letter-spacing: 0.5px;
        }
        .compare-check { color: #22c55e; }
        .compare-cross { color: #6b7280; }
      `}</style>

      {step === 1 && (
        <>
          <div className="text-center mb-5">
            <h1 className="text-white fw-bold mb-2">⚡ Nâng Cấp Tài Khoản GUTIM PRO 👑</h1>
            <p className="text-light-50">Mở khoá đầy đủ các tính năng thông minh để tối ưu hoá hiệu quả tập luyện.</p>
          </div>

          <div className="compare-table mb-5 mx-auto" style={{ maxWidth: 720 }}>
            <table className="table table-dark table-borderless mb-0">
              <thead>
                <tr>
                  <th>Tính năng</th>
                  <th className="text-center">Free</th>
                  <th className="text-center text-warning">PRO</th>
                </tr>
              </thead>
              <tbody className="text-light">
                <tr>
                  <td>Chat với Gutim Coach</td>
                  <td className="text-center">3 tin / ngày</td>
                  <td className="text-center"><span className="compare-check">✓</span> Không giới hạn</td>
                </tr>
                <tr>
                  <td>Tìm phòng tập</td>
                  <td className="text-center">Bán kính 1 km</td>
                  <td className="text-center"><span className="compare-check">✓</span> Lên tới 10 km</td>
                </tr>
                <tr>
                  <td>Gợi ý bài tập thông minh</td>
                  <td className="text-center"><span className="compare-cross">—</span></td>
                  <td className="text-center"><span className="compare-check">✓</span></td>
                </tr>
                <tr>
                  <td>Huy hiệu VIP PRO</td>
                  <td className="text-center"><span className="compare-cross">—</span></td>
                  <td className="text-center"><span className="compare-check">✓</span></td>
                </tr>
              </tbody>
            </table>
          </div>

          <Row className="justify-content-center g-4">
            {/* Monthly Plan */}
            <Col md={5} lg={4}>
              <Card className="upgrade-card h-100 p-4 d-flex flex-column">
                <h4 className="text-white mb-2">{plans.monthly.name}</h4>
                <p className="text-light-50 small mb-3">{plans.monthly.desc}</p>
                <div className="my-3">
                  <span className="price-text">{plans.monthly.priceFormatted}</span>
                  <span className="text-light-50">/{plans.monthly.duration}</span>
                </div>
                <hr className="border-secondary my-3" />
                <div className="flex-grow-1 mb-4">
                  <div className="feature-item">✅ 💬 Chat không giới hạn với Gutim Coach</div>
                  <div className="feature-item">✅ 🗺️ Tìm phòng tập bán kính lên tới 10km</div>
                  <div className="feature-item">✅ 🔮 Gợi ý bài tập nâng cao tùy biến</div>
                  <div className="feature-item">✅ 👑 Huy hiệu VIP PRO nổi bật</div>
                </div>
                <Button 
                  variant="outline-light" 
                  onClick={() => handleSelectPlan("monthly")}
                  className="w-100 py-2.5 fw-bold mt-auto"
                >
                  Chọn gói Tháng
                </Button>
              </Card>
            </Col>

            {/* Yearly Plan */}
            <Col md={5} lg={4}>
              <Card className="upgrade-card best-value h-100 p-4 d-flex flex-column">
                <div className="plan-badge">{plans.yearly.badge}</div>
                <h4 className="text-white mb-2">{plans.yearly.name}</h4>
                <p className="text-light-50 small mb-3">{plans.yearly.desc}</p>
                <div className="my-3">
                  <span className="price-text">{plans.yearly.priceFormatted}</span>
                  <span className="text-light-50">/{plans.yearly.duration}</span>
                </div>
                <hr className="border-secondary my-3" />
                <div className="flex-grow-1 mb-4">
                  <div className="feature-item text-orange">🚀 Đầy đủ quyền lợi Pro toàn diện</div>
                  <div className="feature-item">✅ 💬 Chat không giới hạn với Gutim Coach</div>
                  <div className="feature-item">✅ 🗺️ Tìm phòng tập bán kính lên tới 10km</div>
                  <div className="feature-item">✅ 🔮 Gợi ý bài tập nâng cao tùy biến</div>
                  <div className="feature-item">✅ 👑 Huy hiệu VIP PRO nổi bật</div>
                  <div className="feature-item text-warning">⭐️ Hỗ trợ kỹ thuật 24/7</div>
                </div>
                <Button 
                  variant="warning" 
                  onClick={() => handleSelectPlan("yearly")}
                  className="w-100 py-2.5 fw-bold text-dark mt-auto"
                >
                  Đăng ký gói Năm
                </Button>
              </Card>
            </Col>
          </Row>
        </>
      )}

      {step === 2 && selectedPlan && (
        <Row className="justify-content-center">
          <Col md={10} lg={8}>
            <div className="mb-4">
              <Button variant="link" onClick={() => setStep(1)} className="text-light p-0 text-decoration-none">
                ← Chọn gói khác
              </Button>
            </div>
            
            <Row className="g-4 align-items-start">
              {/* Payment Info */}
              <Col md={6}>
                <h3 className="text-white mb-3">Hướng dẫn thanh toán</h3>
                <div className="qr-card text-light">
                  {/* Step-by-step guide */}
                  <div className="mb-4 p-3" style={{ background: "rgba(255,107,53,0.06)", border: "1px solid rgba(255,107,53,0.15)", borderRadius: "12px" }}>
                    <div className="fw-bold text-orange small mb-2">Hướng dẫn 3 bước:</div>
                    <div className="d-flex align-items-start gap-2 mb-2">
                      <span className="badge bg-warning text-dark" style={{ minWidth: "24px" }}>1</span>
                      <span className="small">Mở ứng dụng ngân hàng hoặc quét mã QR bên cạnh</span>
                    </div>
                    <div className="d-flex align-items-start gap-2 mb-2">
                      <span className="badge bg-warning text-dark" style={{ minWidth: "24px" }}>2</span>
                      <span className="small">Chuyển khoản đúng số tiền và nội dung bên dưới</span>
                    </div>
                    <div className="d-flex align-items-start gap-2">
                      <span className="badge bg-warning text-dark" style={{ minWidth: "24px" }}>3</span>
                      <span className="small">Bấm nút <strong>"Xác nhận đã chuyển khoản"</strong> để hệ thống xác minh</span>
                    </div>
                  </div>

                  <div className="mb-3">
                    <span className="text-light-50 text-uppercase small d-block">Gói đăng ký</span>
                    <span className="fw-bold fs-5 text-orange">{selectedPlan.name}</span>
                  </div>
                  <div className="mb-3">
                    <span className="text-light-50 text-uppercase small d-block">Ngân hàng</span>
                    <span className="fw-semibold">🏦 Ngân hàng Quân Đội (MBBank)</span>
                  </div>
                  <div className="mb-3">
                    <span className="text-light-50 text-uppercase small d-block">Số tài khoản</span>
                    <span className="fw-semibold font-monospace fs-5 text-warning">0382766336</span>
                  </div>
                  <div className="mb-3">
                    <span className="text-light-50 text-uppercase small d-block">Chủ tài khoản</span>
                    <span className="fw-semibold">GUTIM FITNESS SYSTEM</span>
                  </div>
                  <div className="mb-3">
                    <span className="text-light-50 text-uppercase small d-block">Số tiền</span>
                    <span className="fw-bold text-orange fs-4">{selectedPlan.priceFormatted}</span>
                  </div>
                  <div className="mb-4">
                    <span className="text-light-50 text-uppercase small d-block">Nội dung chuyển khoản</span>
                    <span className="fw-bold font-monospace bg-dark text-white p-2 rounded d-inline-block mt-1">
                      GUTIM_PRO_{user.username}
                    </span>
                  </div>

                  {/* Demo disclaimer */}
                  <div className="mb-3 p-2 text-center" style={{ background: "rgba(13,110,253,0.08)", border: "1px solid rgba(13,110,253,0.2)", borderRadius: "8px" }}>
                    <small className="text-info">
                      ℹ️ Đây là môi trường demo — giao dịch được mô phỏng để trải nghiệm.
                    </small>
                  </div>

                  {error && <Alert variant="danger">{error}</Alert>}

                  <Button 
                    variant="warning" 
                    onClick={handleConfirmPayment} 
                    className="w-100 py-3 fw-bold text-dark"
                    disabled={loading}
                  >
                    {loading ? (
                      <>
                        <Spinner animation="border" size="sm" className="me-2" />
                        Đang xác minh giao dịch...
                      </>
                    ) : (
                      "Xác nhận đã chuyển khoản"
                    )}
                  </Button>
                </div>
              </Col>

              {/* VietQR Dynamic Code */}
              <Col md={6} className="text-center">
                <Card className="p-4 border-0" style={{ background: "#111a2b", borderRadius: "16px" }}>
                  <h5 className="text-white mb-3">Mã QR Thanh Toán Nhanh</h5>
                  <div className="bg-white p-3 rounded d-inline-block mx-auto mb-3" style={{ boxShadow: "0 8px 24px rgba(0,0,0,0.3)" }}>
                    <img 
                      src={`https://img.vietqr.io/image/mb-0382766336-compact2.png?amount=${selectedPlan.price}&addInfo=GUTIM_PRO_${user.username}&accountName=GUTIM%20FITNESS%20SYSTEM`}
                      alt="VietQR Chuyển khoản"
                      style={{ width: "260px", height: "260px", objectFit: "contain" }}
                    />
                  </div>
                  <p className="text-light-50 small mb-0 px-3">
                    Mở ứng dụng Mobile Banking của bạn, chọn quét mã QR để điền tự động số tài khoản, số tiền và nội dung chuyển khoản.
                  </p>
                </Card>
              </Col>
            </Row>
          </Col>
        </Row>
      )}

      {step === 3 && (
        <div className="text-center py-5" data-aos="zoom-in">
          <div style={{ maxWidth: "600px", margin: "0 auto" }}>
            <div className="fs-1 mb-3 text-warning">👑 🎉</div>
            <h1 className="text-white fw-bold mb-3">Chúc mừng bạn đã nâng cấp thành công!</h1>
            <p className="text-light-50 fs-5 mb-4">
              Tài khoản của bạn đã được chuyển sang chế độ <strong>GUTIM PRO</strong>.
            </p>
            <div className="text-start mx-auto mb-4" style={{ maxWidth: "360px" }}>
              <div className="mb-2 d-flex align-items-center gap-2 text-light">
                <span className="text-success">✅</span> Chat không giới hạn với Gutim Coach
              </div>
              <div className="mb-2 d-flex align-items-center gap-2 text-light">
                <span className="text-success">✅</span> Tìm phòng tập bán kính lên tới 10km
              </div>
              <div className="mb-2 d-flex align-items-center gap-2 text-light">
                <span className="text-success">✅</span> Gợi ý bài tập thông minh nâng cao
              </div>
              <div className="mb-2 d-flex align-items-center gap-2 text-light">
                <span className="text-success">✅</span> Huy hiệu VIP PRO nổi bật
              </div>
            </div>
            <Button variant="warning" onClick={() => navigate("/")} className="px-5 py-3 fw-bold text-dark fs-5">
              Khám Phá GUTIM PRO Ngay
            </Button>
          </div>
        </div>
      )}
    </Container>
  );
};

export default Upgrade;
