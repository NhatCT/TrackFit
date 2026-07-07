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

  useEffect(() => {
    const hash = window.location.hash;
    if (hash) {
      const params = new URLSearchParams(hash.substring(1));
      const idToken = params.get("id_token");
      if (idToken) {
        window.location.hash = "";
        handleGoogleLoginSuccess({ credential: idToken });
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleCustomGoogleLogin = () => {
    const clientId = process.env.REACT_APP_GOOGLE_CLIENT_ID || "335759714856-lsc5f03oik3v423b0u69p2h817rmr596.apps.googleusercontent.com";
    const redirectUri = `${window.location.origin}/login`;
    const nonce = Math.random().toString(36).substring(2) + Date.now().toString(36);
    const scope = "openid email profile";
    const googleAuthUrl = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${clientId}&redirect_uri=${encodeURIComponent(redirectUri)}&response_type=id_token&scope=${encodeURIComponent(scope)}&nonce=${nonce}`;
    window.location.href = googleAuthUrl;
  };

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
        <div className="mt-2 text-center d-flex justify-content-center">
          <Button
            type="button"
            onClick={handleCustomGoogleLogin}
            disabled={loading}
            style={{
              background: "rgba(255,255,255,0.06)",
              border: "1px solid rgba(255,255,255,0.12)",
              color: "#fff",
              fontWeight: "600",
              padding: "10px 16px",
              borderRadius: "10px",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              gap: "10px",
              width: "100%",
              transition: "all 0.2s"
            }}
          >
            <svg viewBox="0 0 24 24" width="20" height="20">
              <path
                fill="#4285F4"
                d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
              />
              <path
                fill="#34A853"
                d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
              />
              <path
                fill="#FBBC05"
                d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
              />
              <path
                fill="#EA4335"
                d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
              />
            </svg>
            Đăng nhập bằng Google
          </Button>
        </div>
      </Form>
    </div>
  );
};

export default Login;