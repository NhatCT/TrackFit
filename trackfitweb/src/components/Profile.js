// src/components/Profile.jsx
import { useEffect, useMemo, useRef, useState, useContext } from "react";
import { authApis, endpoints } from "../configs/Apis";
import { Card, Button, Row, Col, Badge, Form, Alert } from "react-bootstrap";
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

  const [, dispatch] = useContext(MyUserContext);

  // Đổi avatar
  const [msg, setMsg] = useState("");
  const [msgVariant, setMsgVariant] = useState("info");
  const fileRef = useRef();
  const [previewUrl, setPreviewUrl] = useState("");

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

  useEffect(() => {
    load();
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
                    <Badge bg={roleColor} className="align-middle">
                      {roleText}
                    </Badge>
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
    </div>
  );
};

export default Profile;
