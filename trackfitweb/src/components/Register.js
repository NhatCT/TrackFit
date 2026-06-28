
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
    <div className="form-container">
      <Form
        onSubmit={register}
        style={{
          width: "71.5%",
          backgroundColor: "#add8e678",
          margin: "0 auto",
          padding: "2rem",
          boxShadow: "5px 5px 5px #55555599",
          borderRadius: "1rem",
          fontWeight: "bold",
        }}
      >
        <h1 className="text-center mb-3" style={{ color: "#0e3a57" }}>
          ĐĂNG KÝ
        </h1>

        {msg && <Alert variant="danger">{msg}</Alert>}

        {/* Cột trái/phải cho base fields */}
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

        {/* Optional fields (gender, birthDate) */}
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
        <h5 className="mt-3" style={{ color: "#0e3a57" }}>Thông tin sức khỏe (tuỳ chọn)</h5>
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
        <h5 className="mt-2" style={{ color: "#0e3a57" }}>Mục tiêu luyện tập (tuỳ chọn)</h5>
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
        <Form.Group className="mb-3" controlId="avatar">
          <Form.Label>Ảnh đại diện</Form.Label>
          <Form.Control type="file" ref={avatar} accept="image/png,image/jpeg" />
          <Form.Text>Chỉ hỗ trợ JPEG hoặc PNG</Form.Text>
        </Form.Group>

        {loading ? (
          <MySpinner />
        ) : (
          <Form.Group className="mb-3" style={{ direction: "rtl" }}>
            <Button
              type="submit"
              style={{ backgroundColor: "#0e3a57", color: "white", border: "none" }}
              className="mt-2"
            >
              Đăng ký
            </Button>
          </Form.Group>
        )}
      </Form>
    </div>
  );
};

export default Register;
