// src/components/Login.js
import { useContext, useState } from "react";
import { Alert, Button, Form } from "react-bootstrap";
import MySpinner from "./layout/MySpinner";
import { useNavigate } from "react-router-dom";
import Apis, { authApis, endpoints } from "../configs/Apis";
import cookie from "react-cookies";
import { MyUserContext } from "../configs/Context";

const Login = () => {
  const [, dispatch] = useContext(MyUserContext);
  const [user, setUser] = useState({ username: "", password: "" });
  const [msg, setMsg] = useState("");
  const [loading, setLoading] = useState(false);
  const nav = useNavigate();

  const onRegisterClick = () => nav("/register");

  const login = async (e) => {
    e.preventDefault();
    setMsg("");
    setLoading(true);

    try {
      const res = await Apis.post(endpoints.login, {
        username: user.username,
        password: user.password,
      });

      if (res.status === 200) {
        cookie.save("token", res.data.token, { path: "/" });

        const profileRes = await authApis().get(endpoints.profile());
        const profileData = profileRes.data || {};

        cookie.save("user", JSON.stringify(profileData), { path: "/" });
        dispatch({ type: "login", payload: profileData });

        nav("/");
      }
    } catch (ex) {
      const errorMsg =
        ex.response?.data?.message ||
        (ex.response?.status === 401 ? "Sai thông tin đăng nhập!" : "Đăng nhập thất bại!");
      setMsg(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="form-container login-form">
      <Form onSubmit={login}>
        <div className="text-center mb-4">
          <div style={{ fontSize: "2.5rem", marginBottom: "0.5rem" }}>💪</div>
          <h2 className="fw-bold text-white mb-1">Chào mừng trở lại</h2>
          <p className="text-muted small mb-0">Đăng nhập để tiếp tục hành trình sức khỏe</p>
        </div>

        {msg && <Alert variant="danger">{msg}</Alert>}

        {[
          { title: "Tên đăng nhập", field: "username", type: "text", icon: "👤" },
          { title: "Mật khẩu", field: "password", type: "password", icon: "🔒" },
        ].map((i) => (
          <Form.Group key={i.field} className="mb-3" controlId={i.field}>
            <Form.Label>{i.icon} {i.title}</Form.Label>
            <Form.Control
              required
              value={user[i.field] || ""}
              onChange={(e) => setUser((prev) => ({ ...prev, [i.field]: e.target.value }))}
              type={i.type}
              placeholder={`Nhập ${i.title.toLowerCase()}`}
            />
          </Form.Group>
        ))}

        {loading ? (
          <div className="text-center py-2"><MySpinner /></div>
        ) : (
          <div className="d-grid gap-2 mt-4">
            <Button variant="primary" type="submit" size="lg" className="fw-bold py-2">
              Đăng Nhập
            </Button>
            <div className="text-center mt-2">
              <span className="text-muted small">Chưa có tài khoản? </span>
              <Button variant="link" onClick={onRegisterClick} className="p-0 text-decoration-none" style={{ color: "var(--accent)" }}>
                Đăng ký ngay
              </Button>
            </div>
          </div>
        )}
      </Form>
    </div>
  );
};

export default Login;