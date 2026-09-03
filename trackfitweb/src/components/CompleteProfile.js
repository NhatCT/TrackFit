import { useContext, useState } from "react";
import { Alert, Button, Form } from "react-bootstrap";
import { authApis, authCookieOptions, endpoints } from "../configs/Apis";
import { useNavigate } from "react-router-dom";
import MySpinner from "./layout/MySpinner";
import cookie from "react-cookies";
import { MyUserContext } from "../configs/Context";

const CompleteProfile = () => {
  const [, dispatch] = useContext(MyUserContext);
  const [formData, setFormData] = useState({
    height: "",
    weight: "",
    gender: "Male",
    birthDate: "",
    goalType: "general_fitness",
    intensity: "Medium",
  });
  const [msg, setMsg] = useState("");
  const [loading, setLoading] = useState(false);
  const nav = useNavigate();

  const validate = () => {
    const h = Number(formData.height);
    const w = Number(formData.weight);
    if (Number.isNaN(h) || h < 50 || h > 250) {
      setMsg("Chiều cao phải nằm trong khoảng từ 50cm đến 250cm");
      return false;
    }
    if (Number.isNaN(w) || w < 20 || w > 300) {
      setMsg("Cân nặng phải nằm trong khoảng từ 20kg đến 300kg");
      return false;
    }
    if (!formData.birthDate) {
      setMsg("Vui lòng chọn ngày sinh");
      return false;
    }
    const birth = new Date(formData.birthDate);
    if (birth >= new Date()) {
      setMsg("Ngày sinh phải ở quá khứ");
      return false;
    }
    setMsg("");
    return true;
  };

  const submit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    setLoading(true);

    try {
      const res = await authApis().post(endpoints.completeProfile, {
        height: Number(formData.height),
        weight: Number(formData.weight),
        gender: formData.gender,
        birthDate: formData.birthDate,
        goalType: formData.goalType,
        intensity: formData.intensity,
      });

      if (res.status === 200) {
        // Fetch updated profile
        const profileRes = await authApis().get(endpoints.profile());
        const profileData = profileRes.data || {};

        cookie.save("user", JSON.stringify(profileData), authCookieOptions);
        dispatch({ type: "login", payload: profileData });

        nav("/");
      }
    } catch (ex) {
      const errorMsg = ex.response?.data?.message || "Hoàn tất hồ sơ thất bại!";
      setMsg(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="container my-5 text-white"
      style={{
        maxWidth: "600px",
        background: "rgba(15, 23, 42, 0.95)",
        padding: "40px",
        borderRadius: "16px",
        border: "1px solid rgba(255, 255, 255, 0.08)",
        boxShadow: "0 16px 48px rgba(0,0,0,0.5)",
      }}
    >
      <Form onSubmit={submit}>
        <h1 className="text-center mb-4 text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-purple-500 font-extrabold" style={{ fontSize: "2rem", letterSpacing: "1px" }}>
          THIẾT LẬP THỂ TRẠNG
        </h1>
        <p className="text-center text-gray-400 mb-4" style={{ fontSize: "0.9rem", color: "#94a3b8" }}>
          Chào mừng bạn! Vui lòng cung cấp các chỉ số thể trạng để AI phân tích và xây dựng lộ trình luyện tập phù hợp nhất.
        </p>

        {msg && <Alert variant="danger">{msg}</Alert>}

        <Form.Group className="mb-3" controlId="gender">
          <Form.Label>Giới tính</Form.Label>
          <Form.Select
            value={formData.gender}
            onChange={(e) => setFormData((p) => ({ ...p, gender: e.target.value }))}
            style={{ background: "#0f172a", color: "#fff", borderColor: "rgba(255,255,255,0.1)" }}
          >
            <option value="Male">Nam (Male)</option>
            <option value="Female">Nữ (Female)</option>
          </Form.Select>
        </Form.Group>

        <Form.Group className="mb-3" controlId="birthDate">
          <Form.Label>Ngày sinh</Form.Label>
          <Form.Control
            required
            type="date"
            value={formData.birthDate}
            onChange={(e) => setFormData((p) => ({ ...p, birthDate: e.target.value }))}
            style={{ background: "#0f172a", color: "#fff", borderColor: "rgba(255,255,255,0.1)" }}
          />
        </Form.Group>

        <Form.Group className="mb-3" controlId="height">
          <Form.Label>Chiều cao (cm)</Form.Label>
          <Form.Control
            required
            type="number"
            step="0.1"
            min="50"
            max="250"
            placeholder="Ví dụ: 172.5"
            value={formData.height}
            onChange={(e) => setFormData((p) => ({ ...p, height: e.target.value }))}
            style={{ background: "#0f172a", color: "#fff", borderColor: "rgba(255,255,255,0.1)" }}
          />
        </Form.Group>

        <Form.Group className="mb-3" controlId="weight">
          <Form.Label>Cân nặng (kg)</Form.Label>
          <Form.Control
            required
            type="number"
            step="0.1"
            min="20"
            max="300"
            placeholder="Ví dụ: 68.2"
            value={formData.weight}
            onChange={(e) => setFormData((p) => ({ ...p, weight: e.target.value }))}
            style={{ background: "#0f172a", color: "#fff", borderColor: "rgba(255,255,255,0.1)" }}
          />
        </Form.Group>

        <Form.Group className="mb-3" controlId="goalType">
          <Form.Label>Mục tiêu luyện tập</Form.Label>
          <Form.Select
            value={formData.goalType}
            onChange={(e) => setFormData((p) => ({ ...p, goalType: e.target.value }))}
            style={{ background: "#0f172a", color: "#fff", borderColor: "rgba(255,255,255,0.1)" }}
          >
            <option value="fat_loss">Giảm mỡ (Fat Loss)</option>
            <option value="muscle_gain">Tăng cơ (Muscle Gain)</option>
            <option value="endurance">Sức bền (Endurance)</option>
            <option value="flexibility">Dẻo dai (Flexibility)</option>
            <option value="general_fitness">Thể lực chung (General Fitness)</option>
          </Form.Select>
        </Form.Group>

        <Form.Group className="mb-4" controlId="intensity">
          <Form.Label>Cường độ vận động mong muốn</Form.Label>
          <Form.Select
            value={formData.intensity}
            onChange={(e) => setFormData((p) => ({ ...p, intensity: e.target.value }))}
            style={{ background: "#0f172a", color: "#fff", borderColor: "rgba(255,255,255,0.1)" }}
          >
            <option value="Low">Thấp (Low)</option>
            <option value="Medium">Trung bình (Medium)</option>
            <option value="High">Cao (High)</option>
          </Form.Select>
        </Form.Group>

        <div className="d-grid mt-4">
          {loading ? (
            <MySpinner />
          ) : (
            <Button
              variant="primary"
              type="submit"
              style={{
                background: "linear-gradient(135deg, #2563eb, #7c3aed)",
                border: "none",
                fontWeight: "700",
                padding: "12px",
                letterSpacing: "1px",
              }}
            >
              HOÀN TẤT & KHÁM PHÁ NGAY
            </Button>
          )}
        </div>
      </Form>
    </div>
  );
};

export default CompleteProfile;
