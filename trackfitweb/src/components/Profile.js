// src/components/Profile.jsx
import { useEffect, useMemo, useRef, useState, useContext } from "react";
import { authApis, endpoints } from "../configs/Apis";
import { Card, Button, Row, Col, Badge, Form, Alert, Spinner } from "react-bootstrap";
import { Link } from "react-router-dom";
import AOS from "aos";
import "aos/dist/aos.css";
import { MyUserContext } from "../configs/Context";

const fmtDate = (d) => {
  if (!d) return "Chưa cập nhật";
  try {
    const dt = new Date(d);
    if (Number.isNaN(dt.getTime())) return d;
    return dt.toLocaleDateString("vi-VN", { year: "numeric", month: "2-digit", day: "2-digit" });
  } catch {
    return d;
  }
};

const normGender = (g) => {
  if (!g) return "Chưa cập nhật";
  const s = String(g).toLowerCase();
  if (["male", "nam"].includes(s)) return "Nam";
  if (["female", "nữ", "nu"].includes(s)) return "Nữ";
  return g;
};

const MAX_AVATAR_MB = 3;
const ACCEPTS = "image/png,image/jpeg,image/jpg,image/webp";

const Profile = () => {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  const [user, dispatch] = useContext(MyUserContext);

  // Đổi avatar
  const [msg, setMsg] = useState("");
  const [msgVariant, setMsgVariant] = useState("info");
  const fileRef = useRef();
  const [previewUrl, setPreviewUrl] = useState("");

  const [achievements, setAchievements] = useState({
    streak: 0,
    plansCount: 0,
    goalsCount: 0,
    healthCount: 0,
  });
  const [achMapLoading, setAchMapLoading] = useState(true);

  useEffect(() => {
    AOS.init({ duration: 700, once: true });
  }, []);

  const load = async () => {
    setLoading(true);
    try {
      const res = await authApis().get(endpoints.profile());
      setProfile(res.data);
    } catch (err) {
      console.error("Không tải được hồ sơ:", err);
    } finally {
      setLoading(false);
    }
  };

  const loadAchievements = async () => {
    setAchMapLoading(true);
    try {
      const [histRes, plansRes, goalsRes, healthRes] = await Promise.all([
        authApis().get(endpoints.histories, { params: { status: "COMPLETED", pageSize: 0 } }),
        authApis().get(endpoints.plans),
        authApis().get(endpoints.goals),
        authApis().get(endpoints.health),
      ]);

      const histories = histRes.data?.items || [];
      const plans = Array.isArray(plansRes.data) ? plansRes.data : plansRes.data?.items || [];
      const goals = Array.isArray(goalsRes.data) ? goalsRes.data : goalsRes.data?.items || [];
      const health = Array.isArray(healthRes.data) ? healthRes.data : healthRes.data?.items || [];

      // Calculate streak
      const completedDates = new Set(
        histories
          .filter((h) => h.completedAt)
          .map((h) => new Date(h.completedAt).toDateString())
      );

      let currentStreak = 0;
      const today = new Date();
      const todayStr = today.toDateString();
      const yesterday = new Date();
      yesterday.setDate(yesterday.getDate() - 1);
      const yesterdayStr = yesterday.toDateString();

      const hasCompletedToday = completedDates.has(todayStr);
      const hasCompletedYesterday = completedDates.has(yesterdayStr);

      if (hasCompletedToday) {
        currentStreak = 1;
        const curr = new Date();
        while (true) {
          curr.setDate(curr.getDate() - 1);
          if (completedDates.has(curr.toDateString())) {
            currentStreak++;
          } else {
            break;
          }
        }
      } else if (hasCompletedYesterday) {
        currentStreak = 1;
        const curr = new Date();
        curr.setDate(curr.getDate() - 1);
        while (true) {
          curr.setDate(curr.getDate() - 1);
          if (completedDates.has(curr.toDateString())) {
            currentStreak++;
          } else {
            break;
          }
        }
      }

      setAchievements({
        streak: currentStreak,
        plansCount: plans.length,
        goalsCount: goals.length,
        healthCount: health.length,
      });
    } catch (err) {
      console.error("Lỗi khi tải thành tựu:", err);
    } finally {
      setAchMapLoading(false);
    }
  };

  useEffect(() => {
    load();
    loadAchievements();
  }, []);

  const { roleText, roleColor } = useMemo(() => {
    const r = String(profile?.role || "").toUpperCase();
    const isAdmin = /ADMIN$/.test(r);
    return {
      roleText: isAdmin ? "Quản trị viên" : "Người dùng",
      roleColor: isAdmin ? "danger" : "secondary",
    };
  }, [profile]);

  const onPickFile = (e) => {
    setMsg("");
    const f = e.target.files?.[0];
    if (!f) return setPreviewUrl("");
    const okType = ACCEPTS.split(",").some((t) => f.type === t.trim());
    if (!okType) {
      setMsgVariant("warning");
      return setMsg("Định dạng không hợp lệ. Hãy chọn PNG/JPG/WebP.");
    }
    if (f.size > MAX_AVATAR_MB * 1024 * 1024) {
      setMsgVariant("warning");
      return setMsg(`Ảnh quá lớn. Tối đa ${MAX_AVATAR_MB}MB.`);
    }
    setPreviewUrl(URL.createObjectURL(f));
  };

  const submitAvatar = async (e) => {
    e.preventDefault();
    setMsg("");
    const f = fileRef.current?.files?.[0];
    if (!f) {
      setMsgVariant("warning");
      return setMsg("Vui lòng chọn ảnh!");
    }
    const form = new FormData();
    form.append("avatar", f);
    try {
      await authApis().post(endpoints.changeAvatar, form, {
        headers: { "Content-Type": "multipart/form-data" },
      });

      // Lấy hồ sơ mới để có avatarUrl mới
      const pRes = await authApis().get(endpoints.profile());
      const newUrl = pRes?.data?.avatarUrl || "";
      setProfile(pRes?.data || profile);
      setPreviewUrl("");

      // Cập nhật Context -> Header nhận avatar mới ngay + bust cache
      if (newUrl) {
        dispatch({ type: "updateAvatar", payload: newUrl });
      }

      setMsgVariant("success");
      setMsg("Cập nhật ảnh đại diện thành công.");
    } catch (err) {
      console.error(err);
      setMsgVariant("danger");
      setMsg("Cập nhật ảnh đại diện thất bại. Vui lòng thử lại.");
    }
  };

  if (loading) return <p className="text-center my-4">Đang tải hồ sơ...</p>;
  if (!profile) return <p className="text-center my-4 text-muted">Không có dữ liệu hồ sơ.</p>;

  return (
    <div className="container" data-aos="fade-up" style={{ maxWidth: 920 }}>
      {/* Top bar */}
      <div className="d-flex justify-content-between align-items-center mb-3" data-aos="fade-right">
        <div className="d-flex align-items-center gap-2">
          <h3 className="m-0">Hồ sơ cá nhân</h3>
        </div>
        <div className="d-flex align-items-center gap-2">
          {!user?.isPremium ? (
            <Button as={Link} to="/upgrade" variant="warning" className="fw-bold text-dark me-2">
              Nâng cấp PRO 👑
            </Button>
          ) : (
            <Button as={Link} to="/upgrade" variant="outline-warning" className="fw-bold me-2">
              Gói dịch vụ PRO 👑
            </Button>
          )}
          <Button as={Link} to="/profile/password" variant="outline-primary">
            Đổi mật khẩu
          </Button>
        </div>
      </div>

      {/* Thông tin + Avatar */}
      <Card className="shadow-sm border-0 mb-3 hoverable" data-aos="fade-up">
        <Card.Body>
          <Row className="g-4">
            <Col md={4} className="text-center">
              <div className="d-inline-block">
                <img
                  src={previewUrl || profile.avatarUrl || "https://via.placeholder.com/240x240?text=Avatar"}
                  alt="Avatar"
                  style={{
                    width: 200,
                    height: 200,
                    objectFit: "cover",
                    borderRadius: "50%",
                    boxShadow: "0 6px 18px rgba(0,0,0,0.08)",
                  }}
                />
              </div>

              {/* Form đổi avatar ngay trong Profile */}
              <Form className="mt-3 text-start" onSubmit={submitAvatar}>
                {msg && <Alert variant={msgVariant}>{msg}</Alert>}
                <Form.Group controlId="avatarFile" className="mb-2">
                  <Form.Label className="small text-uppercase text-muted">Đổi ảnh đại diện</Form.Label>
                  <Form.Control type="file" ref={fileRef} accept={ACCEPTS} onChange={onPickFile} />
                  <div className="form-text">PNG/JPG/WebP • Tối đa {MAX_AVATAR_MB}MB</div>
                </Form.Group>
                <div className="d-flex justify-content-end">
                  <Button type="submit" variant="primary">
                    Lưu ảnh
                  </Button>
                </div>
              </Form>
            </Col>

            <Col md={8}>
              <Row className="g-3">
                <Col sm={6}>
                  <div className="small text-uppercase text-muted">Họ tên</div>
                  <div className="fw-semibold fs-5">
                    {(profile.lastName || "") + " " + (profile.firstName || "")}
                  </div>
                </Col>
                <Col sm={6}>
                  <div className="small text-uppercase text-muted">Quyền</div>
                  <div>
                    <Badge bg={roleColor} className="align-middle me-2">
                      {roleText}
                    </Badge>
                    {user?.isPremium && (
                      <Badge bg="warning" className="align-middle text-dark fw-bold" style={{ boxShadow: "0 0 8px rgba(255,193,7,0.5)" }}>
                        👑 PRO MEMBER
                      </Badge>
                    )}
                  </div>
                </Col>

                <Col sm={6}>
                  <div className="small text-uppercase text-muted">Email</div>
                  <div className="fw-semibold">{profile.email || "Chưa cập nhật"}</div>
                </Col>
                <Col sm={6}>
                  <div className="small text-uppercase text-muted">Tên đăng nhập</div>
                  <div className="fw-semibold">{profile.username || "—"}</div>
                </Col>

                <Col sm={6}>
                  <div className="small text-uppercase text-muted">Giới tính</div>
                  <div>{normGender(profile.gender)}</div>
                </Col>
                <Col sm={6}>
                  <div className="small text-uppercase text-muted">Ngày sinh</div>
                  <div>{fmtDate(profile.birthDate)}</div>
                </Col>

                <Col sm={12}>
                  <div className="small text-uppercase text-muted">Cập nhật</div>
                  <div className="text-muted">
                    Tạo lúc: {fmtDate(profile.createdAt)} • Sửa gần nhất: {fmtDate(profile.updatedAt)}
                  </div>
                </Col>
              </Row>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Huy hiệu & Thành tựu */}
      <Card className="shadow-sm border-0 mb-4 bg-surface text-light mt-4" data-aos="fade-up">
        <Card.Body>
          <Card.Title className="text-white border-bottom border-secondary pb-2 mb-3 d-flex justify-content-between align-items-center">
            <span>🏆 Huy hiệu Thành tựu</span>
            <span className="badge bg-warning text-dark fs-6" style={{ color: "#000" }}>
              Đã đạt: {
                [
                  achievements.streak >= 1,
                  achievements.streak >= 7,
                  achievements.streak >= 30,
                  achievements.plansCount > 0,
                  achievements.goalsCount > 0,
                  achievements.healthCount >= 3
                ].filter(Boolean).length
              }/6
            </span>
          </Card.Title>
          
          {achMapLoading ? (
            <div className="text-center py-4">
              <Spinner animation="border" variant="warning" size="sm" className="me-2" />
              Đang tải danh sách huy hiệu...
            </div>
          ) : (
            <Row className="g-3">
              {[
                {
                  id: "streak_bronze",
                  title: "Huy hiệu Khởi động",
                  desc: "Tập luyện liên tục 1 ngày",
                  emoji: "🔥",
                  achieved: achievements.streak >= 1,
                  metric: `${achievements.streak}/1 ngày`,
                  gradient: "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)",
                },
                {
                  id: "streak_silver",
                  title: "Huy hiệu Chăm chỉ",
                  desc: "Tập luyện liên tục 7 ngày",
                  emoji: "💪",
                  achieved: achievements.streak >= 7,
                  metric: `${achievements.streak}/7 ngày`,
                  gradient: "linear-gradient(135deg, #5ee7df 0%, #b490ca 100%)",
                },
                {
                  id: "streak_gold",
                  title: "Huy hiệu Chiến thần",
                  desc: "Tập luyện liên tục 30 ngày",
                  emoji: "🏆",
                  achieved: achievements.streak >= 30,
                  metric: `${achievements.streak}/30 ngày`,
                  gradient: "linear-gradient(135deg, #f6d365 0%, #fda085 100%)",
                },
                {
                  id: "master_trainer",
                  title: "Đệ nhất huấn luyện viên",
                  desc: "Lên kế hoạch tập luyện (kế hoạch > 0)",
                  emoji: "📋",
                  achieved: achievements.plansCount > 0,
                  metric: `${achievements.plansCount}/1 kế hoạch`,
                  gradient: "linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%)",
                },
                {
                  id: "iron_will",
                  title: "Quyết tâm sắt đá",
                  desc: "Thiết lập mục tiêu sức khỏe (mục tiêu > 0)",
                  emoji: "🎯",
                  achieved: achievements.goalsCount > 0,
                  metric: `${achievements.goalsCount}/1 mục tiêu`,
                  gradient: "linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)",
                },
                {
                  id: "health_tracker",
                  title: "Theo dõi sức khỏe",
                  desc: "Theo dõi cân nặng & BMI (chỉ số > 2)",
                  emoji: "❤️",
                  achieved: achievements.healthCount >= 3,
                  metric: `${achievements.healthCount}/3 chỉ số`,
                  gradient: "linear-gradient(135deg, #ff9a9e 0%, #fecfef 99%, #fecfef 100%)",
                },
              ].map((b) => (
                <Col md={4} sm={6} key={b.id}>
                  <div 
                    className="p-3 h-100 d-flex flex-column align-items-center text-center rounded position-relative"
                    style={{
                      backgroundColor: b.achieved ? "rgba(31, 45, 71, 0.6)" : "rgba(17, 26, 43, 0.4)",
                      border: b.achieved ? "1px solid #ff6b35" : "1px solid #1f2d47",
                      opacity: b.achieved ? 1 : 0.55,
                      filter: b.achieved ? "none" : "grayscale(70%)",
                      transition: "all 0.3s ease",
                      cursor: "default"
                    }}
                    onMouseEnter={(e) => {
                      if (b.achieved) {
                        e.currentTarget.style.transform = "translateY(-4px)";
                        e.currentTarget.style.boxShadow = "0 8px 24px rgba(255, 107, 53, 0.25)";
                      }
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.transform = "none";
                      e.currentTarget.style.boxShadow = "none";
                    }}
                  >
                    {/* Badge Icon circle */}
                    <div 
                      className="d-flex align-items-center justify-content-center rounded-circle mb-3 shadow"
                      style={{
                        width: 64,
                        height: 64,
                        fontSize: "2rem",
                        background: b.achieved ? b.gradient : "#1f2d47",
                        border: "2px solid #fff"
                      }}
                    >
                      {b.emoji}
                    </div>

                    <h6 className="text-white mb-1 fw-bold">{b.title}</h6>
                    <p className="text-light-50 small mb-2 flex-grow-1" style={{ fontSize: "0.75rem" }}>{b.desc}</p>
                    
                    <div className="d-flex align-items-center gap-2 mt-2">
                      <span className="badge bg-dark text-muted font-monospace" style={{ fontSize: "0.7rem" }}>
                        {b.metric}
                      </span>
                      {b.achieved ? (
                        <span className="badge bg-success text-white" style={{ fontSize: "0.7rem" }}>Đạt được</span>
                      ) : (
                        <span className="badge bg-secondary text-light-50" style={{ fontSize: "0.7rem" }}>Chưa đạt</span>
                      )}
                    </div>
                  </div>
                </Col>
              ))}
            </Row>
          )}
        </Card.Body>
      </Card>
    </div>
  );
};

export default Profile;
