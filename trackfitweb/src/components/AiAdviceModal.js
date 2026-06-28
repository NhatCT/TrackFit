// src/components/modals/AiAdviceModal.jsx
import { useContext, useEffect, useState } from "react";
import { Button, Form, Modal, Row, Col, InputGroup } from "react-bootstrap";
import { authApis, endpoints } from "../configs/Apis";
import { MyUserContext } from "../configs/Context";

const INTENSITY_OPTS = ["", "Low", "Medium", "High"];
const GOAL_OPTS = ["", "fat_loss", "muscle_gain", "endurance", "flexibility", "general"];

export default function AiAdviceModal({ show, onHide, onCreated }) {
  const [userCtx] = useContext(MyUserContext);

  // user dropdown
  const [userKw, setUserKw] = useState("");
  const [userOptions, setUserOptions] = useState([]);
  const [username, setUsername] = useState(userCtx?.username || "");

  // form params
  const [top, setTop] = useState(3);
  const [withinDays, setWithinDays] = useState(1);
  const [kw, setKw] = useState("");
  const [availableMinutes, setAvailableMinutes] = useState("");
  const [intensity, setIntensity] = useState("");
  const [goalType, setGoalType] = useState("");

  const [loadingUsers, setLoadingUsers] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const fetchUsers = async (needle = "") => {
    try {
      setLoadingUsers(true);
      const r = await authApis().get(endpoints.adminUsers(needle, 100));
      const items = r?.data?.items || [];
      setUserOptions(items);
      // nếu đang trống username, auto chọn bản thân (nếu có trong list)
      if (!username && userCtx?.username) {
        const me = items.find(it => it.username === userCtx.username);
        if (me) setUsername(me.username);
      }
    } catch (e) {
      console.error(e);
      setUserOptions([]);
    } finally {
      setLoadingUsers(false);
    }
  };

  useEffect(() => {
    if (show) fetchUsers("");
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [show]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!username?.trim()) {
      alert("Vui lòng chọn username!");
      return;
    }
    const t = Number(top) || 3;
    const w = Number(withinDays);
    if (w < 0) {
      alert("withinDays phải >= 0");
      return;
    }

    const body = {};
    if (kw) body.kw = kw;
    if (availableMinutes !== "" && !Number.isNaN(Number(availableMinutes)))
      body.availableMinutes = Number(availableMinutes);
    if (intensity) body.intensity = intensity;
    if (goalType) body.goalType = goalType;

    try {
      setSubmitting(true);
      const url = endpoints.aiAdviceFromReco(username.trim(), t, w);
      const r = await authApis().post(url, body);
      const created = Number(r?.data?.created ?? 0);
      if (created > 0) {
        alert(`✅ Đã tạo ${created} thông báo lời khuyên cho "${username}".`);
      } else {
        alert(`ℹ️ Không tạo thêm thông báo (có thể trùng trong withinDays hoặc không có gợi ý phù hợp).`);
      }
      onCreated?.(created);
      onHide?.();
    } catch (err) {
      console.error(err);
      alert("❌ Gọi AI tạo lời khuyên thất bại. Kiểm tra quyền ADMIN, token và server AI.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal show={show} onHide={onHide} centered backdrop="static">
      <Form onSubmit={handleSubmit}>
        <Modal.Header closeButton>
          <Modal.Title>AI tạo lời khuyên</Modal.Title>
        </Modal.Header>

        <Modal.Body>
          <Row className="g-3">
            <Col xs={12}>
              <Form.Label>Chọn username</Form.Label>
              <InputGroup className="mb-2">
                <Form.Control
                  placeholder="Tìm theo username/họ tên…"
                  value={userKw}
                  onChange={(e) => setUserKw(e.target.value)}
                  onKeyDown={(e) => e.key === "Enter" && fetchUsers(userKw)}
                />
                <Button variant="outline-primary" onClick={() => fetchUsers(userKw)} disabled={loadingUsers}>
                  {loadingUsers ? "Đang tìm…" : "Tìm"}
                </Button>
              </InputGroup>

              <Form.Select
                value={username}
                onChange={(e) => setUsername(e.target.value)}
              >
                <option value="">— Chọn username —</option>
                {userOptions.map((u) => (
                  <option key={u.userId} value={u.username}>
                    {u.username}
                    {u.fullName ? ` — ${u.fullName}` : ""} {u.role ? ` [${u.role}]` : ""}
                  </option>
                ))}
              </Form.Select>
            </Col>

            <Col md={6}>
              <Form.Label>Số gợi ý (top)</Form.Label>
              <Form.Control
                type="number"
                min={1}
                value={top}
                onChange={(e) => setTop(e.target.value)}
              />
            </Col>

            <Col md={6}>
              <Form.Label>Chống trùng trong (ngày)</Form.Label>
              <InputGroup>
                <Form.Control
                  type="number"
                  min={0}
                  value={withinDays}
                  onChange={(e) => setWithinDays(e.target.value)}
                />
                <InputGroup.Text>ngày</InputGroup.Text>
              </InputGroup>
              <Form.Text className="text-muted">
                0 = bỏ chống trùng; &gt;0 = không lặp nội dung tương tự trong N ngày
              </Form.Text>
            </Col>

            <Col md={6}>
              <Form.Label>Từ khoá bài tập (tuỳ chọn)</Form.Label>
              <Form.Control
                placeholder="vd: cardio, push, yoga…"
                value={kw}
                onChange={(e) => setKw(e.target.value)}
              />
            </Col>

            <Col md={6}>
              <Form.Label>Thời gian rảnh (phút, tuỳ chọn)</Form.Label>
              <Form.Control
                type="number"
                min={0}
                placeholder="vd: 20"
                value={availableMinutes}
                onChange={(e) => setAvailableMinutes(e.target.value)}
              />
            </Col>

            <Col md={6}>
              <Form.Label>Cường độ (tuỳ chọn)</Form.Label>
              <Form.Select value={intensity} onChange={(e) => setIntensity(e.target.value)}>
                {INTENSITY_OPTS.map((v) => (
                  <option key={v} value={v}>
                    {v || "— Chưa chọn —"}
                  </option>
                ))}
              </Form.Select>
            </Col>

            <Col md={6}>
              <Form.Label>Mục tiêu (tuỳ chọn)</Form.Label>
              <Form.Select value={goalType} onChange={(e) => setGoalType(e.target.value)}>
                {GOAL_OPTS.map((v) => (
                  <option key={v} value={v}>
                    {v || "— Chưa chọn —"}
                  </option>
                ))}
              </Form.Select>
            </Col>
          </Row>
        </Modal.Body>

        <Modal.Footer>
          <Button variant="outline-secondary" onClick={onHide} disabled={submitting}>
            Đóng
          </Button>
          <Button type="submit" variant="warning" disabled={submitting}>
            {submitting ? "Đang tạo..." : "Tạo ngay"}
          </Button>
        </Modal.Footer>
      </Form>
    </Modal>
  );
}
