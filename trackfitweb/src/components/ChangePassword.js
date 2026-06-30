// src/components/ChangePassword.jsx
import { useState } from "react";
import { Alert, Button, Form } from "react-bootstrap";
import { authApis, endpoints } from "../configs/Apis";
import { Link } from "react-router-dom";

const ChangePassword = () => {
  const [form, setForm] = useState({ oldPassword: "", newPassword: "", confirmNew: "" });
  const [msg, setMsg] = useState("");
  const [variant, setVariant] = useState("info");
  const [loading, setLoading] = useState(false);

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
    <div className="form-container">
      <Form onSubmit={submit}>
        <div className="d-flex justify-content-between align-items-center mb-4">
          <h4 className="m-0 fw-bold text-white">🔐 Đổi mật khẩu</h4>
          <Button as={Link} to="/profile" variant="outline-secondary" size="sm">
            Về hồ sơ
          </Button>
        </div>

        {msg && <Alert variant={variant}>{msg}</Alert>}

        <Form.Group className="mb-3">
          <Form.Label>Mật khẩu cũ</Form.Label>
          <Form.Control
            type="password"
            value={form.oldPassword}
            onChange={(e) => setForm({ ...form, oldPassword: e.target.value })}
            placeholder="Nhập mật khẩu hiện tại"
            required
          />
        </Form.Group>
        <Form.Group className="mb-3">
          <Form.Label>Mật khẩu mới</Form.Label>
          <Form.Control
            type="password"
            value={form.newPassword}
            onChange={(e) => setForm({ ...form, newPassword: e.target.value })}
            placeholder="Tối thiểu 6 ký tự"
            minLength={6}
            required
          />
        </Form.Group>
        <Form.Group className="mb-4">
          <Form.Label>Xác nhận mật khẩu mới</Form.Label>
          <Form.Control
            type="password"
            value={form.confirmNew}
            onChange={(e) => setForm({ ...form, confirmNew: e.target.value })}
            placeholder="Nhập lại mật khẩu mới"
            minLength={6}
            required
          />
        </Form.Group>
        <div className="d-grid">
          <Button type="submit" variant="primary" disabled={loading} className="fw-bold py-2">
            {loading ? "Đang lưu..." : "Cập nhật mật khẩu"}
          </Button>
        </div>
      </Form>
    </div>
  );
};

export default ChangePassword;
