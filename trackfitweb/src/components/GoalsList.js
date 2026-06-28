// src/components/GoalsList.js
import { useEffect, useMemo, useState } from "react";
import { Alert, Badge, Button, Card, Form, Table } from "react-bootstrap";
import { authApis, endpoints } from "../configs/Apis";
import MySpinner from "./layout/MySpinner";


const INTENSITY_OPTS = ["Low", "Medium", "High"];
const EMPTY = { goalType: "", intensity: "Medium", workoutDuration: 30 };

const GoalsList = () => {
  const [items, setItems] = useState([]);
  const [kw, setKw] = useState("");
  const [form, setForm] = useState(EMPTY);
  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState({ type: "", text: "" });

  const load = async () => {
    setLoading(true);
    try {
      const r = await authApis().get(endpoints.goals); // /api/secure/goals
      const data = Array.isArray(r.data) ? r.data : (r.data?.items || []);
      setItems(data);
    } catch (e) {
      setMsg({ type: "danger", text: e?.response?.data?.message || "Tải mục tiêu thất bại" });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const filtered = useMemo(() => {
    if (!kw.trim()) return items;
    const q = kw.toLowerCase();
    return items.filter((g) =>
      (g.name || g.goalType || "").toLowerCase().includes(q) ||
      String(g.goalId || "").includes(q) ||
      String(g.workoutDuration || "").includes(q) ||
      (g.intensity || "").toLowerCase().includes(q)
    );
  }, [items, kw]);

  const submit = async (e) => {
    e.preventDefault();
    setMsg({ type: "", text: "" });

    // vệ sinh dữ liệu
    const payload = {
      goalType: form.goalType.trim(),
      intensity: form.intensity,
      workoutDuration: Math.max(1, parseInt(form.workoutDuration, 10) || 0),
    };

    try {
      await authApis().post(endpoints.goals, payload);
      setForm(EMPTY);
      setMsg({ type: "success", text: "Thêm mục tiêu thành công" });
      load();
    } catch (e) {
      const text =
        e?.response?.data?.message ||
        "Lưu thất bại. (Lưu ý: intensity phải là Low/Medium/High; workoutDuration 1–365)";
      setMsg({ type: "danger", text });
    }
  };

  const del = async (id) => {
    if (!window.confirm("Xóa mục tiêu này?")) return;
    try {
      await authApis().delete(`${endpoints.goals}/${id}`);
      setMsg({ type: "success", text: "Đã xóa mục tiêu" });
      load();
    } catch (e) {
      setMsg({ type: "danger", text: e?.response?.data?.message || "Xóa thất bại" });
    }
  };

  return (
    <>
      <div className="d-flex align-items-center justify-content-between mb-3">
        <div className="d-flex align-items-center gap-2">
          <h3 className="m-0">Mục tiêu</h3>
          {!!items?.length && <Badge bg="secondary">{items.length}</Badge>}
        </div>

        <Form
          className="d-flex gap-2"
          onSubmit={(e) => {
            e.preventDefault();
          }}
        >
          <Form.Control
            className="search-pill"
            placeholder="Tìm theo tên/ID/intensity/duration…"
            value={kw}
            onChange={(e) => setKw(e.target.value)}
            style={{ minWidth: 260 }}
          />
          <Button
            variant="outline-secondary"
            type="button"
            onClick={() => setKw("")}
          >
            Xoá lọc
          </Button>
        </Form>
      </div>

      {/* Form thêm mới */}
      <Card className="shadow-sm border-0 mb-3 hoverable plan-card">
        <Card.Header className="bg-white">
          <strong>Thêm mục tiêu</strong>
        </Card.Header>
        <Card.Body>
          {msg.text && <Alert variant={msg.type || "info"}>{msg.text}</Alert>}

          <Form onSubmit={submit} className="row g-3">
            <div className="col-md-4">
              <Form.Label>Loại mục tiêu</Form.Label>
              <Form.Control
                placeholder="Ví dụ: Giảm cân / Tăng cơ…"
                value={form.goalType}
                onChange={(e) => setForm({ ...form, goalType: e.target.value })}
                required
              />
              <Form.Text className="text-muted">Tối đa 50 ký tự.</Form.Text>
            </div>

            <div className="col-md-3">
              <Form.Label>Cường độ</Form.Label>
              <Form.Select
                value={form.intensity}
                onChange={(e) => setForm({ ...form, intensity: e.target.value })}
              >
                {INTENSITY_OPTS.map((x) => (
                  <option key={x} value={x}>{x}</option>
                ))}
              </Form.Select>
              <Form.Text className="text-muted">Chỉ nhận Low / Medium / High.</Form.Text>
            </div>

            <div className="col-md-3">
              <Form.Label>Thời lượng (ngày)</Form.Label>
              <Form.Control
                type="number"
                min={1}
                max={365}
                value={form.workoutDuration}
                onChange={(e) => setForm({ ...form, workoutDuration: e.target.value })}
              />
              <Form.Text className="text-muted">Trong khoảng 1–365.</Form.Text>
            </div>

            <div className="col-md-2 d-grid align-items-end">
              <Button type="submit">Thêm</Button>
            </div>
          </Form>
        </Card.Body>
      </Card>

      {/* Bảng danh sách */}
      {loading ? (
        <MySpinner />
      ) : (
        <Card className="shadow-sm border-0">
          <Table hover responsive className="m-0 align-middle">
            <thead className="table-light">
              <tr>
                <th style={{ width: 90 }}>ID</th>
                <th>Tên hiển thị</th>
                <th>Goal type</th>
                <th style={{ width: 120 }}>Intensity</th>
                <th style={{ width: 150 }}>Duration</th>
                <th style={{ width: 120 }}></th>
              </tr>
            </thead>
            <tbody>
              {filtered?.length ? (
                filtered.map((g) => (
                  <tr key={g.goalId}>
                    <td>#{g.goalId}</td>
                    <td className="fw-semibold">{g.name || "-"}</td>
                    <td>{g.goalType || "-"}</td>
                    <td>
                      {g.intensity ? (
                        <Badge bg={g.intensity === "High" ? "danger" : g.intensity === "Medium" ? "warning" : "success"}>
                          {g.intensity}
                        </Badge>
                      ) : (
                        "-"
                      )}
                    </td>
                    <td>{g.workoutDuration ? `${g.workoutDuration} ngày` : "-"}</td>
                    <td className="text-end">
                      <Button
                        size="sm"
                        variant="outline-danger"
                        onClick={() => del(g.goalId)}
                      >
                        Xóa
                      </Button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={6} className="text-center text-muted py-4">
                    Chưa có mục tiêu
                  </td>
                </tr>
              )}
            </tbody>
          </Table>
        </Card>
      )}
    </>
  );
};

export default GoalsList;
