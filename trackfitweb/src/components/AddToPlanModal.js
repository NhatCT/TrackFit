// src/components/plan/AddToPlanModal.jsx
import { useEffect, useMemo, useState } from "react";
import { Alert, Badge, Button, Form, Modal, Row, Col, Spinner } from "react-bootstrap";
import { authApis, endpoints } from "../configs/Apis";

const dayOptions = [
  { v: 2, label: "Thứ 2" },
  { v: 3, label: "Thứ 3" },
  { v: 4, label: "Thứ 4" },
  { v: 5, label: "Thứ 5" },
  { v: 6, label: "Thứ 6" },
  { v: 7, label: "Thứ 7" },
  { v: 8, label: "Chủ nhật" },
];

export default function AddToPlanModal({ show, onHide, exercise, defaultDuration = 30, onAdded }) {
  const [loadingPlans, setLoadingPlans] = useState(false);
  const [plans, setPlans] = useState([]);
  const [kw, setKw] = useState("");
  const [selectedPlanId, setSelectedPlanId] = useState("");
  const [dayOfWeek, setDayOfWeek] = useState(2);
  const [duration, setDuration] = useState(defaultDuration);
  const [saving, setSaving] = useState(false);
  const [msg, setMsg] = useState("");
  const [err, setErr] = useState("");

  // Tạo nhanh kế hoạch
  const [creating, setCreating] = useState(false);
  const [newPlanName, setNewPlanName] = useState("");

  const loadPlans = async () => {
    setLoadingPlans(true);
    setErr(""); setMsg("");
    try {
      const r = await authApis().get(endpoints.plans, { params: { page: 1, pageSize: 100 } });
      const items = Array.isArray(r.data) ? r.data : (r.data?.items || []);
      setPlans(items);
      if (!items.length) setSelectedPlanId("");
    } catch {
      setErr("Không tải được danh sách kế hoạch.");
    } finally {
      setLoadingPlans(false);
    }
  };

  useEffect(() => {
    if (show) {
      loadPlans();
      setSelectedPlanId("");
      setDuration(defaultDuration);
      setDayOfWeek(2);
      setKw("");
      setNewPlanName("");
      setCreating(false);
      setMsg(""); setErr("");
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [show, defaultDuration]);

  const filteredPlans = useMemo(() => {
    if (!kw.trim()) return plans;
    const q = kw.toLowerCase();
    return plans.filter(
      (p) =>
        p.planName?.toLowerCase?.().includes(q) ||
        String(p.planId).includes(q) ||
        p.userName?.toLowerCase?.().includes(q)
    );
  }, [plans, kw]);

  const createQuickPlan = async () => {
    if (!newPlanName.trim()) {
      setErr("Vui lòng nhập tên kế hoạch mới!");
      return;
    }
    setCreating(true); setErr(""); setMsg("");
    try {
      const r = await authApis().post(endpoints.plans, {
        planName: newPlanName.trim(),
        isTemplate: false,
        goalId: null,
      });
      setMsg("Đã tạo kế hoạch mới.");
      await loadPlans();
      setSelectedPlanId(r.data?.planId || "");
      setNewPlanName("");
    } catch (e) {
      setErr(e?.response?.data?.message || "Tạo kế hoạch thất bại.");
    } finally {
      setCreating(false);
    }
  };

  const addToPlan = async () => {
    if (!exercise?.exercisesId && !exercise?.exerciseId) {
      setErr("Thiếu thông tin bài tập.");
      return;
    }
    if (!selectedPlanId) {
      setErr("Vui lòng chọn một kế hoạch.");
      return;
    }
    setSaving(true); setErr(""); setMsg("");
    try {
      await authApis().post(endpoints.planAddDetail(selectedPlanId), {
        exerciseId: +(exercise.exercisesId ?? exercise.exerciseId),
        dayOfWeek: +dayOfWeek,
        duration: +duration,
      });
      setMsg("Đã thêm vào kế hoạch.");
      onAdded && onAdded();
      setTimeout(() => onHide?.(), 600);
    } catch (e) {
      setErr(e?.response?.data?.message || "Thêm vào kế hoạch thất bại.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal show={show} onHide={onHide} size="lg" centered>
      <Modal.Header closeButton>
        <Modal.Title>Thêm vào kế hoạch</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <div className="mb-3">
          <div className="fw-semibold">{exercise?.name || `Bài tập #${exercise?.exercisesId}`}</div>
          <div className="text-muted small">
            ID: {exercise?.exercisesId ?? exercise?.exerciseId}{" "}
            {exercise?.muscleGroup && <Badge bg="dark" className="ms-2">{exercise.muscleGroup}</Badge>}
          </div>
        </div>

        {err && <Alert variant="danger">{err}</Alert>}
        {msg && <Alert variant="success">{msg}</Alert>}

        <Row className="g-3">
          <Col md={8}>
            <Form.Label>Kế hoạch của bạn</Form.Label>
            <div className="d-flex gap-2">
              <Form.Control
                placeholder="Tìm theo tên/ID kế hoạch..."
                value={kw}
                onChange={(e) => setKw(e.target.value)}
              />
              <Button variant="outline-secondary" onClick={loadPlans} disabled={loadingPlans}>
                {loadingPlans ? <Spinner size="sm" /> : "Làm mới"}
              </Button>
            </div>
            <Form.Select
              className="mt-2"
              value={selectedPlanId}
              onChange={(e) => setSelectedPlanId(e.target.value)}
            >
              <option value="">— Chọn kế hoạch —</option>
              {filteredPlans.map((p) => (
                <option key={p.planId} value={p.planId}>
                  {p.planName} (#{p.planId}) {p.goalName ? `• ${p.goalName}` : ""}
                </option>
              ))}
            </Form.Select>
            {!plans.length && (
              <div className="text-muted small mt-2">Bạn chưa có kế hoạch nào, hãy tạo mới bên dưới.</div>
            )}
          </Col>

          <Col md={4}>
            <Form.Label>Ngày & thời lượng</Form.Label>
            <Form.Select className="mb-2" value={dayOfWeek} onChange={(e) => setDayOfWeek(e.target.value)}>
              {dayOptions.map((d) => (
                <option key={d.v} value={d.v}>{d.label}</option>
              ))}
            </Form.Select>
            <Form.Control
              type="number"
              min={1}
              value={duration}
              onChange={(e) => setDuration(e.target.value)}
              placeholder="Phút"
            />
          </Col>
        </Row>

        <hr />

        <Form.Label>Tạo nhanh kế hoạch (nếu chưa có)</Form.Label>
        <div className="d-flex gap-2">
          <Form.Control
            placeholder="Nhập tên kế hoạch mới (VD: Giảm mỡ 4 tuần)"
            value={newPlanName}
            onChange={(e) => setNewPlanName(e.target.value)}
          />
          <Button onClick={createQuickPlan} disabled={creating}>
            {creating ? <Spinner size="sm" /> : "Tạo"}
          </Button>
        </div>
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={onHide} disabled={saving}>Đóng</Button>
        <Button variant="primary" onClick={addToPlan} disabled={saving || !selectedPlanId}>
          {saving ? "Đang thêm..." : "Thêm vào kế hoạch"}
        </Button>
      </Modal.Footer>
    </Modal>
  );
}
