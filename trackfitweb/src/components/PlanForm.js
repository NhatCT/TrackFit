import { useEffect, useState, useMemo, useContext } from "react";
import { Alert, Badge, Button, Card, Col, Form, Modal, Row, Table } from "react-bootstrap";
import { useNavigate, useParams, Link } from "react-router-dom";
import { authApis, endpoints } from "../configs/Apis";
import { MyUserContext } from "../configs/Context";
import MySpinner from "./layout/MySpinner";
import "./styles/Plans.css";

const PAGE_SIZE_EX = 8;

const DOW_LABEL = { 1: "T2", 2: "T3", 3: "T4", 4: "T5", 5: "T6", 6: "T7", 7: "CN" };
const dayLabel = (d) => DOW_LABEL[Number(d)] || d;

const PlanForm = () => {
  const { id } = useParams();
  const nav = useNavigate();

  const [userCtx] = useContext(MyUserContext);
  const isAdmin =
    !!(
      userCtx?.role?.toUpperCase?.().includes?.("ADMIN") ||
      userCtx?.roles?.some?.((r) => (typeof r === "string" ? r.toUpperCase().includes("ADMIN") : false)) ||
      userCtx?.authorities?.some?.((a) =>
        (typeof a === "string" ? a : a?.authority)?.toUpperCase?.().includes("ADMIN")
      )
    );

  const [plan, setPlan] = useState({ planName: "", isTemplate: false, goalId: "", details: [] });
  const [msg, setMsg] = useState("");
  const [err, setErr] = useState("");
  const [saving, setSaving] = useState(false);

  // -------- Goals dropdown ----------
  const [goals, setGoals] = useState([]);
  const [goalKw, setGoalKw] = useState("");

  const filteredGoals = useMemo(() => {
    if (!goalKw.trim()) return goals;
    const q = goalKw.toLowerCase();
    return goals.filter((g) => g.name?.toLowerCase().includes(q) || String(g.goalId).includes(q));
  }, [goals, goalKw]);

  useEffect(() => {
    const loadGoals = async () => {
      try {
        const r = await authApis().get(endpoints.goals);
        const items = Array.isArray(r.data) ? r.data : r.data?.items || [];
        setGoals(items);
      } catch {
        // bỏ qua lỗi goals
      }
    };
    loadGoals();
  }, []);

  // -------- Exercises modal ----------
  const [showPick, setShowPick] = useState(false);
  const [kwEx, setKwEx] = useState("");
  const [pageEx, setPageEx] = useState(1);
  const [resEx, setResEx] = useState(null);
  const [loadingEx, setLoadingEx] = useState(false);
  const [newDetail, setNewDetail] = useState({
    exerciseId: "",
    dayOfWeek: 1, // 1 = Thứ 2
    duration: 30,
    exerciseName: "",
  });

  const loadExercises = async () => {
    setLoadingEx(true);
    try {
      const r = await authApis().get(endpoints.exercises, {
        params: { page: pageEx, pageSize: PAGE_SIZE_EX, kw: kwEx || undefined },
      });
      setResEx(r.data);
    } finally {
      setLoadingEx(false);
    }
  };

  const itemsEx = useMemo(() => resEx?.items || [], [resEx]);

  const openPick = () => {
    setShowPick(true);
    setKwEx("");
    setPageEx(1);
    setNewDetail({ exerciseId: "", dayOfWeek: 1, duration: 30, exerciseName: "" });
  };
  const closePick = () => setShowPick(false);

  useEffect(() => {
    if (showPick) loadExercises();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [showPick, pageEx]);

  const pickExercise = (ex) => {
    setNewDetail((d) => ({
      ...d,
      exerciseId: ex.exercisesId,
      exerciseName: ex.name || `#${ex.exercisesId}`,
    }));
  };

  // -------- Load/Save plan ----------
  useEffect(() => {
    const load = async () => {
      if (!id) return;
      try {
        const r = await authApis().get(endpoints.planDetail(id));
        setPlan({
          planName: r.data.planName,
          isTemplate: r.data.isTemplate,
          goalId: r.data.goalId || "",
          details: r.data.details || [],
        });
      } catch {
        setErr("Không tải được kế hoạch.");
      }
    };
    load();
  }, [id]);

  const refreshPlan = async () => {
    if (!id) return;
    const r = await authApis().get(endpoints.planDetail(id));
    setPlan((p) => ({ ...p, details: r.data.details || [] }));
  };

  const savePlan = async (e) => {
    e.preventDefault();
    setMsg("");
    setErr("");
    setSaving(true);
    try {
      const payload = {
        planName: plan.planName?.trim(),
        goalId: plan.goalId ? parseInt(plan.goalId, 10) : null,
        ...(isAdmin ? { isTemplate: !!plan.isTemplate } : {}), // chỉ admin được gửi isTemplate
      };

      if (!id) {
        const r = await authApis().post(endpoints.plans, payload);
        setMsg("Tạo kế hoạch thành công!");
        nav(`/plans/${r.data.planId}`);
      } else {
        await authApis().put(endpoints.planDetail(id), payload);
        setMsg("Cập nhật kế hoạch thành công!");
        await refreshPlan();
      }
    } catch (e2) {
      setErr(e2?.response?.data?.message || "Lưu thất bại");
    } finally {
      setSaving(false);
    }
  };

  const addDetail = async () => {
    if (!id) return setErr("Hãy lưu kế hoạch trước rồi mới thêm bài tập!");
    if (!newDetail.exerciseId) return setErr("Vui lòng chọn bài tập!");
    setErr("");
    setMsg("");
    try {
      await authApis().post(endpoints.planAddDetail(id), {
        exerciseId: Number(newDetail.exerciseId),
        dayOfWeek: Number(newDetail.dayOfWeek), // 1..7
        duration: Number(newDetail.duration),
      });
      await refreshPlan();
      setMsg("Đã thêm bài tập vào kế hoạch.");
      closePick();
    } catch (e) {
      setErr(e?.response?.data?.message || "Thêm chi tiết thất bại");
    }
  };

  const delDetail = async (detailId) => {
    setErr("");
    setMsg("");
    try {
      await authApis().delete(endpoints.planDetailDelete(detailId));
      await refreshPlan();
      setMsg("Đã xóa bài tập khỏi kế hoạch.");
    } catch (e) {
      setErr(e?.response?.data?.message || "Xóa chi tiết thất bại");
    }
  };

  return (
    <>
      <div className="d-flex align-items-center gap-2 mb-3">
        <h3 className="m-0">{id ? "Cập nhật kế hoạch" : "Tạo kế hoạch"}</h3>
        {id && <Badge bg="secondary">#{id}</Badge>}
      </div>

      {msg && <Alert variant="success">{msg}</Alert>}
      {err && <Alert variant="danger">{err}</Alert>}

      <Row className="g-4">
        {/* Thông tin kế hoạch */}
        <Col lg={5}>
          <Card className="shadow-sm border-0 hoverable plan-card">
            <Card.Header className="bg-white">
              <div className="fw-semibold">Thông tin kế hoạch</div>
            </Card.Header>
            <Card.Body>
              <Form onSubmit={savePlan} className="plan-form">
                <Form.Group className="mb-3">
                  <Form.Label>Tên kế hoạch</Form.Label>
                  <Form.Control
                    value={plan.planName}
                    onChange={(e) => setPlan({ ...plan, planName: e.target.value })}
                    placeholder="Ví dụ: Giảm mỡ 4 tuần"
                    required
                  />
                </Form.Group>

                <Form.Group className="mb-2">
                  <div className="d-flex justify-content-between align-items-center">
                    <Form.Label className="mb-0">Mục tiêu (Goal)</Form.Label>
                    <small>
                      <Link to="/goals">Quản lý mục tiêu</Link>
                    </small>
                  </div>
                  <Form.Control
                    className="mb-2"
                    placeholder="Tìm theo tên/ID mục tiêu..."
                    value={goalKw}
                    onChange={(e) => setGoalKw(e.target.value)}
                  />
                  <Form.Select
                    value={plan.goalId || ""}
                    onChange={(e) => setPlan({ ...plan, goalId: e.target.value })}
                  >
                    <option value="">— Không gắn mục tiêu —</option>
                    {filteredGoals.map((g) => (
                      <option key={g.goalId} value={g.goalId}>
                        {g.name} (#{g.goalId})
                      </option>
                    ))}
                  </Form.Select>
                  <Form.Text className="text-muted">
                    Bạn có thể tạo mục tiêu mới ở trang “Goals”, sau đó quay lại chọn.
                  </Form.Text>
                </Form.Group>

                {/* Chỉ ADMIN mới thấy công tắc Template */}
                {isAdmin && (
                  <Row className="g-2 mb-3">
                    <Col md={6} className="d-flex align-items-end">
                      <Form.Check
                        type="switch"
                        id="switch-template"
                        label="Đặt làm Template"
                        checked={!!plan.isTemplate}
                        onChange={(e) => setPlan({ ...plan, isTemplate: e.target.checked })}
                      />
                    </Col>
                  </Row>
                )}

                <div className="text-end">
                  <Button type="submit" disabled={saving}>
                    {saving ? "Đang lưu..." : id ? "Lưu thay đổi" : "Tạo kế hoạch"}
                  </Button>
                </div>
              </Form>
            </Card.Body>
          </Card>
        </Col>

        {/* Danh sách bài tập trong kế hoạch */}
        <Col lg={7}>
          <Card className="shadow-sm border-0 hoverable plan-card">
            <Card.Header className="bg-white d-flex justify-content-between align-items-center">
              <div className="fw-semibold">Bài tập trong kế hoạch</div>
              <Button
                variant="outline-primary"
                onClick={openPick}
                disabled={!id}
                title={!id ? "Hãy lưu kế hoạch trước" : ""}
              >
                + Thêm bài tập
              </Button>
            </Card.Header>
            <Card.Body className="p-0">
              <Table hover responsive className="m-0 align-middle">
                <thead className="table-dark-soft">
                  <tr>
                    <th style={{ width: 80 }}>ID</th>
                    <th>Bài tập</th>
                    <th style={{ width: 100 }}>Thứ</th>
                    <th style={{ width: 90 }}>Phút</th>
                    <th style={{ width: 120 }} className="text-end">
                      Thao tác
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {plan.details?.length ? (
                    plan.details.map((d) => (
                      <tr key={d.detailId}>
                        <td>#{d.detailId}</td>
                        <td>
                          {d.exerciseName || "—"}{" "}
                          <span className="text-muted">#{d.exerciseId}</span>
                        </td>
                        <td>{dayLabel(d.dayOfWeek)}</td>
                        <td>{d.duration}</td>
                        <td className="text-end">
                          <Button
                            size="sm"
                            variant="outline-danger"
                            onClick={() => delDetail(d.detailId)}
                          >
                            Xóa
                          </Button>
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan={5} className="text-center text-muted py-4">
                        Chưa có bài tập. Nhấn <b>“+ Thêm bài tập”</b> để chọn từ danh sách.
                      </td>
                    </tr>
                  )}
                </tbody>
              </Table>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Modal chọn bài tập */}
      <Modal show={showPick} onHide={closePick} size="lg" centered>
        <Modal.Header closeButton>
          <Modal.Title>Chọn bài tập</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form
            className="d-flex flex-column flex-md-row gap-2 mb-3"
            onSubmit={(e) => {
              e.preventDefault();
              setPageEx(1);
              loadExercises();
            }}
          >
            <Form.Control
              className="search-pill"
              placeholder="Từ khoá tên/mô tả bài tập..."
              value={kwEx}
              onChange={(e) => setKwEx(e.target.value)}
            />
            <Button variant="outline-primary" type="submit">
              Tìm
            </Button>
          </Form>

          {loadingEx ? (
            <MySpinner />
          ) : (
            <>
              <div className="exercise-pick-list">
                {itemsEx.length ? (
                  itemsEx.map((x) => (
                    <Card
                      key={x.exercisesId}
                      className={`border-0 shadow-sm pick-card ${
                        Number(newDetail.exerciseId) === Number(x.exercisesId) ? "active" : ""
                      }`}
                      onClick={() => pickExercise(x)}
                    >
                      <Card.Body>
                        <div className="fw-semibold">{x.name}</div>
                        <div className="text-muted small mt-1">ID: {x.exercisesId}</div>
                        <div className="text-muted small">
                          {x.muscleGroup && <Badge bg="dark" className="me-1">{x.muscleGroup}</Badge>}
                          {x.targetGoal && <Badge bg="secondary">{x.targetGoal}</Badge>}
                        </div>
                      </Card.Body>
                    </Card>
                  ))
                ) : (
                  <div className="text-center text-muted py-4">Không có kết quả</div>
                )}
              </div>

              {resEx && resEx.totalPages > 1 && (
                <div className="d-flex gap-2 justify-content-center mt-3">
                  <Button size="sm" disabled={pageEx <= 1} onClick={() => setPageEx((p) => p - 1)}>
                    Trước
                  </Button>
                  <div className="align-self-center small">
                    Trang {pageEx}/{resEx.totalPages}
                  </div>
                  <Button
                    size="sm"
                    disabled={pageEx >= resEx.totalPages}
                    onClick={() => setPageEx((p) => p + 1)}
                  >
                    Sau
                  </Button>
                </div>
              )}

              <hr />
              <Row className="g-2">
                <Col md={4}>
                  <Form.Label>Ngày trong tuần</Form.Label>
                  <Form.Select
                    value={newDetail.dayOfWeek}
                    onChange={(e) =>
                      setNewDetail((d) => ({ ...d, dayOfWeek: Number(e.target.value) }))
                    }
                  >
                    <option value={1}>Thứ 2</option>
                    <option value={2}>Thứ 3</option>
                    <option value={3}>Thứ 4</option>
                    <option value={4}>Thứ 5</option>
                    <option value={5}>Thứ 6</option>
                    <option value={6}>Thứ 7</option>
                    <option value={7}>Chủ nhật</option>
                  </Form.Select>
                </Col>
                <Col md={4}>
                  <Form.Label>Thời lượng (phút)</Form.Label>
                  <Form.Control
                    type="number"
                    min={1}
                    value={newDetail.duration}
                    onChange={(e) =>
                      setNewDetail((d) => ({ ...d, duration: Math.max(1, Number(e.target.value || 1)) }))
                    }
                  />
                </Col>
                <Col md={4} className="d-flex align-items-end">
                  <Button className="w-100" disabled={!newDetail.exerciseId} onClick={addDetail}>
                    Thêm vào kế hoạch
                  </Button>
                </Col>
              </Row>
            </>
          )}
        </Modal.Body>
      </Modal>
    </>
  );
};

export default PlanForm;
