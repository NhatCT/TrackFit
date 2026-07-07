// src/components/Login.js
import { useContext, useState, useEffect } from "react";
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

  const handleGoogleLoginSuccess = async (googleRes) => {
    setLoading(true);
    setMsg("");
    try {
      const res = await Apis.post(endpoints.googleLogin, {
        credential: googleRes.credential,
      });

      if (res.status === 200) {
        cookie.save("token", res.data.token, { path: "/" });

        const profileRes = await authApis().get(endpoints.profile());
        const profileData = profileRes.data || {};

        cookie.save("user", JSON.stringify(profileData), { path: "/" });
        dispatch({ type: "login", payload: profileData });

        if (res.data.isNewUser) {
          nav("/complete-profile");
        } else {
          nav("/");
        }
      }
    } catch (ex) {
      setMsg(ex.response?.data?.message || "Đăng nhập bằng Google thất bại!");
    } finally {
      setLoading(false);
    }
  };

  /* global google */
  useEffect(() => {
    if (window.google) {
      window.google.accounts.id.initialize({
        client_id: process.env.REACT_APP_GOOGLE_CLIENT_ID || "335759714856-lsc5f03oik3v423b0u69p2h817rmr596.apps.googleusercontent.com",
        callback: handleGoogleLoginSuccess,
      });
      window.google.accounts.id.renderButton(
        document.getElementById("google-login-btn"),
        { theme: "outline", size: "large", width: "100%" }
      );
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

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
        {msg && <Alert variant="danger">{msg}</Alert>}
        <h1 className="text-center mb-3">ĐĂNG NHẬP</h1>
        {[
          { title: "Tên đăng nhập", field: "username", type: "text" },
          { title: "Mật khẩu", field: "password", type: "password" },
        ].map((i) => (
          <Form.Group key={i.field} className="mb-3" controlId={i.field}>
            <Form.Label>{i.title}</Form.Label>
            <Form.Control
              required
              value={user[i.field] || ""}
              onChange={(e) => setUser((prev) => ({ ...prev, [i.field]: e.target.value }))}
              type={i.type}
              placeholder={`Nhập ${i.title.toLowerCase()}`}
            />
          </Form.Group>
        ))}
        <div className="d-flex justify-content-end gap-2 mb-3">
          {loading ? (
            <MySpinner />
          ) : (
            <>
              <Button variant="outline-primary" onClick={onRegisterClick}>
                Đăng Kí
              </Button>
              <Button variant="primary" type="submit">
                Đăng Nhập
              </Button>
            </>
          )}
        </div>
        <div className="d-flex align-items-center my-3">
          <hr className="flex-grow-1" style={{ borderColor: "rgba(255,255,255,0.15)" }} />
          <span className="mx-2 text-muted" style={{ fontSize: "0.85rem" }}>Hoặc đăng nhập với</span>
          <hr className="flex-grow-1" style={{ borderColor: "rgba(255,255,255,0.15)" }} />
        </div>
        <div id="google-login-btn" className="mt-2 text-center d-flex justify-content-center"></div>
      </Form>
    </div>
  );
};

export default Login;