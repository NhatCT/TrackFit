// src/components/layout/Header.js
import { useContext, useEffect, useMemo, useRef, useState } from "react";
import { Button, Container, Nav, Navbar, NavDropdown } from "react-bootstrap";
import { Link, useNavigate } from "react-router-dom";
import { MyUserContext } from "../../configs/Context";
import logo from "../../img/logo.png";
import AOS from "aos";
import "aos/dist/aos.css";
import "../styles/Header.css";
import { authApis, endpoints } from "../../configs/Apis";

const AVATAR_FALLBACK = "https://via.placeholder.com/64x64.png?text=👤";

const Header = (props) => {
  // cho phép nhận user/onLogout qua props (App cung cấp), fallback sang Context
  const [ctxUser, dispatch] = useContext(MyUserContext);
  const user = props?.user ?? ctxUser;
  const onLogoutProp = props?.onLogout;
  const nav = useNavigate();

  const [notifCount, setNotifCount] = useState(0);
  const pollRef = useRef(null);

  // Avatar hiển thị cạnh username
  const [avatarUrl, setAvatarUrl] = useState("");

  useEffect(() => {
    AOS.init({ duration: 800, once: true });
  }, []);

  const loadUnread = async () => {
    if (!user?.userId) return;
    try {
      const r = await authApis().get(`${endpoints.notifications}/unread-count`);
      setNotifCount(Number(r?.data?.count ?? 0));
    } catch {
      // ignore
    }
  };

  const loadAvatar = async () => {
    if (!user?.userId) return setAvatarUrl("");
    if (user?.avatarUrl) return setAvatarUrl(user.avatarUrl); // ưu tiên từ Context (đổi là thấy ngay)
    try {
      const res = await authApis().get(endpoints.profile());
      setAvatarUrl(res?.data?.avatarUrl || "");
    } catch {
      // ignore
    }
  };

  // Khi đổi userId: reset/poll badge + thử load avatar
  useEffect(() => {
    if (pollRef.current) {
      clearInterval(pollRef.current);
      pollRef.current = null;
    }

    if (!user?.userId) {
      setNotifCount(0);
      setAvatarUrl("");
      return;
    }

    loadUnread();
    loadAvatar();
    pollRef.current = setInterval(loadUnread, 60000);

    return () => {
      if (pollRef.current) {
        clearInterval(pollRef.current);
        pollRef.current = null;
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.userId]);

  // Sync WebSocket notifications with Header badge
  useEffect(() => {
    if (!user?.userId) return;

    const handleNewNotif = () => {
      setNotifCount((prev) => prev + 1);
    };

    const handleUpdate = () => {
      loadUnread();
    };

    window.addEventListener("trackfit-notification", handleNewNotif);
    window.addEventListener("trackfit-notif-update", handleUpdate);

    return () => {
      window.removeEventListener("trackfit-notification", handleNewNotif);
      window.removeEventListener("trackfit-notif-update", handleUpdate);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.userId]);

  // Lắng nghe thay đổi avatarUrl trong Context để cập nhật ngay
  useEffect(() => {
    if (user?.avatarUrl) setAvatarUrl(user.avatarUrl);
  }, [user?.avatarUrl]);

  const logout = () => {
    if (onLogoutProp) return onLogoutProp();
    dispatch?.({ type: "logout" });
    setNotifCount(0);
    setAvatarUrl("");
    if (pollRef.current) {
      clearInterval(pollRef.current);
      pollRef.current = null;
    }
    nav("/login");
  };

  const onOpenNotifications = async () => {
    try {
      await authApis().put(`${endpoints.notifications}/read-all`);
      setNotifCount(0);
    } catch {
      // ignore
    }
  };

  // Tạo URL có tham số version để bust cache
  const shownAvatar = useMemo(() => {
    const base = avatarUrl || AVATAR_FALLBACK;
    if (!avatarUrl) return base;
    const v = user?.avatarVersion || 0;
    return base + (base.includes("?") ? `&v=${v}` : `?v=${v}`);
  }, [avatarUrl, user?.avatarVersion]);

  // Tiêu đề dropdown: avatar + username
  const accountTitle = (
    <span className="d-inline-flex align-items-center gap-2">
      <img
        src={shownAvatar}
        alt="Avatar"
        style={{
          width: 28,
          height: 28,
          borderRadius: "50%",
          objectFit: "cover",
          boxShadow: "0 2px 8px rgba(0,0,0,0.15)",
        }}
      />
      <span className="text-white d-none d-sm-inline">
        {user?.username || "Tài khoản"}
        {user?.isPremium && (
          <span 
            className="ms-1.5 px-1 py-0.5 rounded text-dark bg-warning fw-bold" 
            style={{ fontSize: "0.6rem", letterSpacing: "0.5px", boxShadow: "0 0 8px rgba(255, 193, 7, 0.6)" }}
            title="Premium Member"
          >
            PRO 👑
          </span>
        )}
      </span>
    </span>
  );

  return (
    <Navbar expand="lg" variant="dark" className="p-3 navbar-elevated app-header-dark">
      <Container>
        <Navbar.Brand
          as={Link}
          to="/"
          className="d-flex align-items-center gap-2 text-white text-decoration-none"
          data-aos="fade-right"
        >
          <img src={logo} alt="TrackFit Logo" style={{ height: 40 }} />
          <span className="fw-bold">GUTIM</span>
        </Navbar.Brand>

        <Navbar.Toggle aria-controls="basic-navbar-nav" style={{ borderColor: "white" }} />
        <Navbar.Collapse id="basic-navbar-nav">
          {user && (
            <Nav className="me-auto" data-aos="fade-up">
              <Nav.Link as={Link} to="/" className="text-white">Trang chủ</Nav.Link>
              <Nav.Link as={Link} to="/exercises" className="text-white">Bài tập</Nav.Link>
              <Nav.Link as={Link} to="/plans" className="text-white">Kế hoạch</Nav.Link>
              <Nav.Link as={Link} to="/histories" className="text-white">Lịch sử</Nav.Link>
              <Nav.Link as={Link} to="/goals" className="text-white">Mục tiêu</Nav.Link>
              <Nav.Link as={Link} to="/health" className="text-white">Sức khỏe</Nav.Link>
              <Nav.Link as={Link} to="/gyms" className="text-white">Phòng tập</Nav.Link>
              <Nav.Link as={Link} to="/recommendations" className="text-white">
                Gợi ý cho bạn
              </Nav.Link>

              <Nav.Link as={Link} to="/stats/summary" className="text-white">Thống kê</Nav.Link>

              {!user?.isPremium && (
                <Nav.Link
                  as={Link}
                  to="/upgrade"
                  className="text-warning fw-bold"
                  style={{ textShadow: "0 0 8px rgba(255, 193, 7, 0.4)" }}
                >
                  Nâng cấp PRO ⚡
                </Nav.Link>
              )}

              <Nav.Link
                as={Link}
                to="/notifications"
                className="text-white position-relative"
                onClick={onOpenNotifications}
              >
                Thông báo
                {notifCount > 0 && (
                  <span
                    className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger"
                    style={{ fontSize: "0.6rem" }}
                  >
                    {notifCount}
                  </span>
                )}
              </Nav.Link>
            </Nav>
          )}

          <Nav data-aos="fade-left">
            {!user ? (
              <>
                <Nav.Link as={Link} to="/login" className="text-white">Đăng nhập</Nav.Link>
                <Button
                  as={Link}
                  to="/register"
                  variant="outline-light"
                  className="ms-2"
                  style={{ borderColor: "#ff6b35", color: "#ff6b35" }}
                >
                  Đăng ký
                </Button>
              </>
            ) : (
              <NavDropdown title={accountTitle} align="end" className="text-white" menuVariant="light">
                <NavDropdown.Item as={Link} to="/profile">Trang cá nhân</NavDropdown.Item>
                <NavDropdown.Item as={Link} to="/profile/password">Đổi mật khẩu</NavDropdown.Item>
                {/* Nếu đã gộp đổi avatar vào Profile, có thể xoá item dưới */}
                {/* <NavDropdown.Item as={Link} to="/profile/avatar">Đổi ảnh đại diện</NavDropdown.Item> */}
                <NavDropdown.Divider />
                <NavDropdown.Item onClick={logout} style={{ color: "#ff6b35" }}>
                  Đăng xuất
                </NavDropdown.Item>
              </NavDropdown>
            )}
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default Header;
