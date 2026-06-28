// src/components/ChangePassword.jsx
import { useEffect, useState } from "react";
import { Alert, Button, Form, Card } from "react-bootstrap";
import { authApis, endpoints } from "../configs/Apis";
import { Link } from "react-router-dom";
import AOS from "aos";
import "aos/dist/aos.css";

const ChangePassword = () => {
  const [form, setForm] = useState({ oldPassword: "", newPassword: "", confirmNew: "" });
  const [msg, setMsg] = useState("");
  const [variant, setVariant] = useState("info");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    AOS.init({ duration: 700, once: true });
  }, []);

  const submit = async (e) => {
    e.preventDefault();
    setMsg("");
    if (form.newPassword.length < 6) {
      setVariant("warning");
      return setMsg("Mật khẩu mới tối thiểu 6 ký tự.");
    }
    if (form.newPassword !== form.confirmNew) {
      setVariant("warning");
      return setMsg("Xác nhận mật khẩu không khớp.");
    }
    try {
      setLoading(true);
      const res = await authApis().post(endpoints.changePassword, {
        oldPassword: form.oldPassword,
        newPassword: form.newPassword,
      });
      setVariant("success");
      setMsg(res.data.message || "Đổi mật khẩu thành công.");
      setForm({ oldPassword: "", newPassword: "", confirmNew: "" });
    } catch {
      setVariant("danger");
      setMsg("Đổi mật khẩu thất bại. Vui lòng kiểm tra lại thông tin.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container" style={{ maxWidth: 640 }}>
      <div className="d-flex justify-content-between align-items-center mb-3" data-aos="fade-right">
        <div className="d-flex align-items-center gap-2">
          <h3 className="m-0">Đổi mật khẩu</h3>
        </div>
        <div className="d-flex align-items-center gap-2">
          <Button as={Link} to="/profile" variant="outline-secondary">
            Về hồ sơ
          </Button>
        </div>
      </div>

      <Card className="shadow-sm border-0 hoverable" data-aos="fade-up">
        <Card.Body>
          {msg && <Alert variant={variant}>{msg}</Alert>}
          <Form onSubmit={submit}>
            <Form.Group className="mb-3">
              <Form.Label className="small text-uppercase text-muted">Mật khẩu cũ</Form.Label>
              <Form.Control
                type="password"
                value={form.oldPassword}
                onChange={(e) => setForm({ ...form, oldPassword: e.target.value })}
                required
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label className="small text-uppercase text-muted">Mật khẩu mới</Form.Label>
              <Form.Control
                type="password"
                value={form.newPassword}
                onChange={(e) => setForm({ ...form, newPassword: e.target.value })}
                minLength={6}
                required
              />
            </Form.Group>
            <Form.Group>
              <Form.Label className="small text-uppercase text-muted">Xác nhận mật khẩu mới</Form.Label>
              <Form.Control
                type="password"
                value={form.confirmNew}
                onChange={(e) => setForm({ ...form, confirmNew: e.target.value })}
                minLength={6}
                required
              />
            </Form.Group>
            <div className="text-end mt-3">
              <Button type="submit" disabled={loading}>
                {loading ? "Đang lưu..." : "Lưu"}
              </Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </div>
  );
};

export default ChangePassword;
