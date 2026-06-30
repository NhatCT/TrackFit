
import { useRef, useState } from "react";
import { Alert, Button, Col, Form, Row } from "react-bootstrap";
import Apis, { endpoints } from "../configs/Apis";
import { useNavigate } from "react-router-dom";
import MySpinner from "./layout/MySpinner";

const Register = () => {
  const baseInfo = [
    { title: "Họ và tên đệm", field: "lastName", type: "text", required: true },
    { title: "Tên", field: "firstName", type: "text", required: true },
    { title: "Email", field: "email", type: "email", required: true },
    { title: "Tên đăng nhập", field: "username", type: "text", required: true },
    { title: "Mật khẩu", field: "password", type: "password", required: true },
    { title: "Xác nhận mật khẩu", field: "confirmPassword", type: "password", required: true },
  ];

  const optionalFields = [
    { title: "Giới tính", field: "gender", type: "select", required: false },
    { title: "Ngày sinh", field: "birthDate", type: "date", required: false },
  ];

  // === Thêm các trường Sức khỏe & Mục tiêu luyện tập ===
  const healthFields = [
    { title: "Chiều cao (cm)", field: "height", type: "number", step: "0.1", min: "0" },
    { title: "Cân nặng (kg)", field: "weight", type: "number", step: "0.1", min: "0" },
  ];

  const goalFields = [
    {
      title: "Mục tiêu",
      field: "goalType",
      type: "select",
      options: [
        { v: "", label: "-- Chọn mục tiêu --" },
        { v: "fat_loss", label: "Giảm mỡ" },
        { v: "muscle_gain", label: "Tăng cơ" },
        { v: "endurance", label: "Sức bền" },
        { v: "flexibility", label: "Dẻo dai" },
        { v: "general_fitness", label: "Thể lực chung" },
      ],
    },
    {
      title: "Cường độ",
      field: "intensity",
      type: "select",
      options: [
        { v: "", label: "-- Chọn cường độ --" },
        { v: "Low", label: "Thấp" },
        { v: "Medium", label: "Trung bình" },
        { v: "High", label: "Cao" },
      ],
    },
  ];

  const avatar = useRef();
  const [user, setUser] = useState({});
  const [msg, setMsg] = useState("");
  const [loading, setLoading] = useState(false);
  const nav = useNavigate();

  const validate = () => {
    if ((user.confirmPassword || "") !== (user.password || "")) {
      setMsg("Mật khẩu không khớp!");
      return false;
    }
    // kiểm tra số hợp lệ nếu có nhập
    const h = user.height !== undefined && user.height !== "" ? Number(user.height) : null;
    const w = user.weight !== undefined && user.weight !== "" ? Number(user.weight) : null;
    if (h !== null && (Number.isNaN(h) || h <= 0)) {
      setMsg("Chiều cao phải > 0");
      return false;
    }
    if (w !== null && (Number.isNaN(w) || w <= 0)) {
      setMsg("Cân nặng phải > 0");
      return false;
    }
    setMsg("");
    return true;
  };

  const register = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    try {
      setLoading(true);
      const formData = new FormData();

      // Gửi đúng tên trường backend cần (bỏ qua field rỗng)
      const toAppend = [...baseInfo, ...optionalFields, ...healthFields, ...goalFields];
      toAppend.forEach((i) => {
        const v = user[i.field];
        if (v !== undefined && v !== null && v !== "") {
          formData.append(i.field, v); // height/weight -> BigDecimal ở BE sẽ parse từ string
        }
      });

      if (avatar.current?.files?.length > 0) {
        formData.append("avatar", avatar.current.files[0]);
      }

      const res = await Apis.post(endpoints.register, formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });

      if (res.status === 201) {
        nav("/login");
      }
    } catch (ex) {
      console.error(ex);
      const serverMsg = ex?.response?.data?.message;
      setMsg(serverMsg || "Đăng ký thất bại! Vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="form-container register-form">
      <Form onSubmit={register}>
        <div className="text-center mb-4">
          <div style={{ fontSize: "2.5rem", marginBottom: "0.5rem" }}>🏋️</div>
          <h2 className="fw-bold text-white mb-1">Tạo tài khoản</h2>
          <p className="text-muted small mb-0">Bắt đầu hành trình sức khỏe cùng GUTIM</p>
        </div>

        {msg && <Alert variant="danger">{msg}</Alert>}

        {/* Thông tin tài khoản */}
        <h6 className="text-uppercase small fw-bold mb-3" style={{ color: "var(--accent)", letterSpacing: "1px" }}>
          Thông tin tài khoản
        </h6>
        <Row>
          <Col md={6}>
            {baseInfo.slice(0, 3).map((i) => (
              <Form.Group key={i.field} className="mb-3" controlId={i.field}>
                <Form.Label>{i.title}</Form.Label>
                <Form.Control
                  required={i.required}
                  value={user[i.field] || ""}
                  onChange={(e) => setUser({ ...user, [i.field]: e.target.value })}
                  type={i.type}
                  placeholder={`Nhập ${i.title.toLowerCase()}`}
                />
              </Form.Group>
            ))}
          </Col>
          <Col md={6}>
            {baseInfo.slice(3).map((i) => (
              <Form.Group key={i.field} className="mb-3" controlId={i.field}>
                <Form.Label>{i.title}</Form.Label>
                <Form.Control
                  required={i.required}
                  value={user[i.field] || ""}
                  onChange={(e) => setUser({ ...user, [i.field]: e.target.value })}
                  type={i.type}
                  placeholder={`Nhập ${i.title.toLowerCase()}`}
                />
              </Form.Group>
            ))}
          </Col>
        </Row>

        {/* Thông tin cá nhân */}
        <hr style={{ borderColor: "var(--border)" }} />
        <h6 className="text-uppercase small fw-bold mb-3" style={{ color: "var(--accent)", letterSpacing: "1px" }}>
          Thông tin cá nhân
        </h6>
        <Row>
          {optionalFields.map((i) => (
            <Col md={6} key={i.field}>
              <Form.Group className="mb-3" controlId={i.field}>
                <Form.Label>{i.title}</Form.Label>
                {i.type === "select" ? (
                  <Form.Select
                    value={user[i.field] || ""}
                    onChange={(e) => setUser({ ...user, [i.field]: e.target.value })}
                  >
                    <option value="">-- Chọn --</option>
                    <option value="Male">Nam</option>
                    <option value="Female">Nữ</option>
                    <option value="Other">Khác</option>
                  </Form.Select>
                ) : (
                  <Form.Control
                    type={i.type}
                    value={user[i.field] || ""}
                    onChange={(e) => setUser({ ...user, [i.field]: e.target.value })}
                  />
                )}
              </Form.Group>
            </Col>
          ))}
        </Row>

        {/* Thông tin Sức khỏe */}
        <hr style={{ borderColor: "var(--border)" }} />
        <h6 className="text-uppercase small fw-bold mb-3" style={{ color: "var(--accent)", letterSpacing: "1px" }}>
          Sức khỏe <span className="text-muted fw-normal">(tuỳ chọn)</span>
        </h6>
        <Row>
          {healthFields.map((i) => (
            <Col md={6} key={i.field}>
              <Form.Group className="mb-3" controlId={i.field}>
                <Form.Label>{i.title}</Form.Label>
                <Form.Control
                  type={i.type}
                  step={i.step || "1"}
                  min={i.min || undefined}
                  value={user[i.field] || ""}
                  onChange={(e) => setUser({ ...user, [i.field]: e.target.value })}
                  placeholder={i.title}
                />
              </Form.Group>
            </Col>
          ))}
        </Row>

        {/* Mục tiêu luyện tập */}
        <hr style={{ borderColor: "var(--border)" }} />
        <h6 className="text-uppercase small fw-bold mb-3" style={{ color: "var(--accent)", letterSpacing: "1px" }}>
          Mục tiêu luyện tập <span className="text-muted fw-normal">(tuỳ chọn)</span>
        </h6>
        <Row>
          {goalFields.map((i) => (
            <Col md={6} key={i.field}>
              <Form.Group className="mb-3" controlId={i.field}>
                <Form.Label>{i.title}</Form.Label>
                <Form.Select
                  value={user[i.field] || ""}
                  onChange={(e) => setUser({ ...user, [i.field]: e.target.value })}
                >
                  {i.options.map((opt) => (
                    <option key={opt.v} value={opt.v}>{opt.label}</option>
                  ))}
                </Form.Select>
              </Form.Group>
            </Col>
          ))}
        </Row>

        {/* Avatar */}
        <hr style={{ borderColor: "var(--border)" }} />
        <Form.Group className="mb-4" controlId="avatar">
          <Form.Label>📸 Ảnh đại diện</Form.Label>
          <Form.Control type="file" ref={avatar} accept="image/png,image/jpeg" />
          <Form.Text>Chỉ hỗ trợ JPEG hoặc PNG</Form.Text>
        </Form.Group>

        {loading ? (
          <div className="text-center py-2"><MySpinner /></div>
        ) : (
          <div className="d-grid gap-2">
            <Button variant="primary" type="submit" size="lg" className="fw-bold py-2">
              Tạo tài khoản
            </Button>
            <div className="text-center mt-2">
              <span className="text-muted small">Đã có tài khoản? </span>
              <Button variant="link" onClick={() => nav("/login")} className="p-0 text-decoration-none" style={{ color: "var(--accent)" }}>
                Đăng nhập
              </Button>
            </div>
          </div>
        )}
      </Form>
    </div>
  );
};

export default Register;
