import { useEffect, useState, useMemo } from "react";
import { Card, Button, Badge, Row, Col, ProgressBar, Form, Alert } from "react-bootstrap";
import { authApis, endpoints } from "../configs/Apis";
import MySpinner from "./layout/MySpinner";

const DOW_LABELS = {
  1: "Thứ Hai",
  2: "Thứ Ba",
  3: "Thứ Tư",
  4: "Thứ Năm",
  5: "Thứ Sáu",
  6: "Thứ Bảy",
  7: "Chủ Nhật",
};

const getTodayDow = () => {
  const day = new Date().getDay(); // 0 (Sun) to 6 (Sat)
  return day === 0 ? 7 : day; // 1 (Mon) to 7 (Sun)
};

export default function TodayWorkout() {
  const [plans, setPlans] = useState([]);
  const [selectedPlanId, setSelectedPlanId] = useState("");
  const [planDetails, setPlanDetails] = useState([]);
  const [historyToday, setHistoryToday] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submittingId, setSubmittingId] = useState(null);
  const [msg, setMsg] = useState("");

  const todayDow = useMemo(() => getTodayDow(), []);
  const todayLabel = useMemo(() => DOW_LABELS[todayDow] || "Hôm nay", [todayDow]);

  const loadPlansAndHistory = async () => {
    setLoading(true);
    setMsg("");
    try {
      // 1. Fetch user plans
      const plansRes = await authApis().get(endpoints.plans, {
        params: { page: 1, pageSize: 100 },
      });
      const userPlans = plansRes.data?.items || [];
      const nonTemplatePlans = userPlans.filter((p) => !p.isTemplate);
      setPlans(nonTemplatePlans);

      // Select first plan by default
      if (nonTemplatePlans.length > 0) {
        const defaultPlanId = nonTemplatePlans[0].planId;
        setSelectedPlanId(defaultPlanId);
        await loadPlanDetailsAndTodayHistory(defaultPlanId);
      } else {
        setLoading(false);
      }
    } catch (e) {
      console.error("Error loading plans:", e);
      setMsg("Không tải được thông tin kế hoạch tập luyện.");
      setLoading(false);
    }
  };

  const loadPlanDetailsAndTodayHistory = async (planId) => {
    try {
      // Fetch plan detail
      const detailRes = await authApis().get(endpoints.planDetail(planId));
      setPlanDetails(detailRes.data?.details || []);

      // Fetch histories for today
      const historyRes = await authApis().get(endpoints.histories, {
        params: { page: 1, pageSize: 100, planId },
      });
      const histories = historyRes.data?.items || [];

      // Filter histories completed today
      const todayStr = new Date().toDateString();
      const todayHistories = histories.filter((h) => {
        if (!h.completedAt) return false;
        return new Date(h.completedAt).toDateString() === todayStr;
      });

      setHistoryToday(todayHistories);
    } catch (e) {
      console.error("Error loading plan details or history:", e);
      setMsg("Có lỗi xảy ra khi tải bài tập hoặc lịch sử.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPlansAndHistory();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handlePlanChange = async (e) => {
    const val = e.target.value;
    setSelectedPlanId(val);
    if (val) {
      setLoading(true);
      await loadPlanDetailsAndTodayHistory(val);
    } else {
      setPlanDetails([]);
      setHistoryToday([]);
    }
  };

  // Filter exercises for today
  const todayExercises = useMemo(() => {
    return planDetails.filter((d) => Number(d.dayOfWeek) === todayDow);
  }, [planDetails, todayDow]);

  // Statistics
  const stats = useMemo(() => {
    if (todayExercises.length === 0) return { total: 0, completed: 0, skipped: 0, percent: 0 };
    const total = todayExercises.length;
    let completed = 0;
    let skipped = 0;

    todayExercises.forEach((ex) => {
      const recorded = historyToday.find((h) => h.exerciseId === ex.exerciseId);
      if (recorded) {
        if (recorded.status === "COMPLETED") completed++;
        else if (recorded.status === "SKIPPED") skipped++;
      }
    });

    const percent = Math.round(((completed + skipped) / total) * 100);
    return { total, completed, skipped, percent };
  }, [todayExercises, historyToday]);

  // Log workout activity
  const logWorkout = async (exerciseId, status, duration) => {
    setSubmittingId(`${exerciseId}-${status}`);
    setMsg("");
    try {
      await authApis().post(endpoints.histories, {
        exerciseId,
        planId: Number(selectedPlanId),
        status,
        duration: duration || 0,
        completedAt: new Date().toISOString(),
      });
      
      // Dispatch a notification event to update other stats or websocket listeners
      window.dispatchEvent(
        new CustomEvent("trackfit-notification", {
          detail: {
            id: Date.now(),
            type: "INFO",
            title: "Cập nhật",
            message: `Đã đánh dấu ${status === "COMPLETED" ? "hoàn thành" : "bỏ qua"} bài tập!`,
          },
        })
      );
      
      // Reload history to update status on UI
      await loadPlanDetailsAndTodayHistory(selectedPlanId);
    } catch (e) {
      console.error("Error logging workout status:", e);
      setMsg("Có lỗi xảy ra khi cập nhật tiến độ.");
    } finally {
      setSubmittingId(null);
    }
  };

  if (loading && plans.length > 0) {
    return (
      <Card className="shadow-sm border-0 mb-4 bg-surface text-light">
        <Card.Body className="text-center py-4">
          <MySpinner />
        </Card.Body>
      </Card>
    );
  }

  return (
    <Card className="shadow-sm border-0 mb-4" style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: "16px" }} data-aos="fade-up">
      <Card.Header className="bg-transparent border-0 pt-4 pb-0 d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3">
        <div>
          <h4 className="fw-bold m-0 text-light">🏋️ Kế hoạch hôm nay ({todayLabel})</h4>
          <p className="text-muted m-0 small">Bắt đầu tập luyện và đánh dấu hoàn thành tiến độ của bạn</p>
        </div>
        
        {plans.length > 1 && (
          <div className="d-flex align-items-center gap-2">
            <span className="text-muted small text-nowrap">Chọn lịch:</span>
            <Form.Select 
              size="sm" 
              value={selectedPlanId} 
              onChange={handlePlanChange} 
              style={{ backgroundColor: "var(--surface-2)", color: "var(--text)", borderColor: "var(--border)", minWidth: "160px" }}
            >
              {plans.map((p) => (
                <option key={p.planId} value={p.planId}>
                  {p.planName}
                </option>
              ))}
            </Form.Select>
          </div>
        )}
      </Card.Header>
      
      <Card.Body className="pt-3">
        {msg && <Alert variant="danger" className="py-2 small">{msg}</Alert>}

        {plans.length === 0 ? (
          <div className="text-center py-4">
            <div className="fs-1 mb-2">📋</div>
            <h5 className="fw-bold text-light">Chưa có kế hoạch tập luyện cá nhân</h5>
            <p className="text-muted small mb-3">Tạo một kế hoạch 7 ngày tùy chỉnh cho mục tiêu của riêng bạn để bắt đầu.</p>
            <Button variant="primary" size="sm" href="/plans/new">
              + Tạo kế hoạch ngay
            </Button>
          </div>
        ) : todayExercises.length === 0 ? (
          <div className="text-center py-4">
            <div className="fs-1 mb-2">😴</div>
            <h5 className="fw-bold text-light">Hôm nay là ngày nghỉ ngơi!</h5>
            <p className="text-muted small mb-0">Hãy nghỉ ngơi đầy đủ để cơ bắp được phục hồi hoặc tham khảo các bài tập nhẹ nhàng.</p>
          </div>
        ) : (
          <>
            {/* Progress bar */}
            <div className="mb-4">
              <div className="d-flex justify-content-between align-items-center mb-1 text-muted small">
                <span>Tiến độ hoàn thành: {stats.completed}/{stats.total} bài tập</span>
                <span className="fw-bold text-light">{stats.percent}%</span>
              </div>
              <ProgressBar 
                now={stats.percent} 
                style={{ height: "8px", backgroundColor: "var(--border)" }}
                variant={stats.percent === 100 ? "success" : "info"}
              />
            </div>

            <Row className="g-3">
              {todayExercises.map((ex) => {
                const recorded = historyToday.find((h) => h.exerciseId === ex.exerciseId);
                const isCompleted = recorded?.status === "COMPLETED";
                const isSkipped = recorded?.status === "SKIPPED";
                
                return (
                  <Col key={ex.detailId} xs={12} md={6}>
                    <Card 
                      className={`h-100 border-0 p-3`} 
                      style={{ 
                        background: isCompleted 
                          ? "rgba(25, 135, 84, 0.08)" 
                          : isSkipped 
                          ? "rgba(108, 117, 125, 0.08)"
                          : "var(--surface-2)",
                        border: isCompleted 
                          ? "1px solid rgba(25, 135, 84, 0.2)" 
                          : "1px solid var(--border)",
                        borderRadius: "12px"
                      }}
                    >
                      <div className="d-flex justify-content-between align-items-start gap-2">
                        <div>
                          <h6 className="fw-bold text-light mb-1">
                            {ex.exerciseName}
                          </h6>
                          <div className="d-flex gap-2 align-items-center flex-wrap mt-1">
                            <Badge bg="secondary" className="bg-opacity-25 text-light-50">⏱️ {ex.duration} phút</Badge>
                            {isCompleted && <Badge bg="success">Hoàn thành</Badge>}
                            {isSkipped && <Badge bg="secondary">Đã bỏ qua</Badge>}
                          </div>
                        </div>
                        
                        <div className="d-flex gap-1">
                          {!isCompleted && !isSkipped ? (
                            <>
                              <Button 
                                size="sm" 
                                variant="primary" 
                                disabled={submittingId !== null}
                                onClick={() => logWorkout(ex.exerciseId, "COMPLETED", ex.duration)}
                              >
                                {submittingId === `${ex.exerciseId}-COMPLETED` ? "..." : "✓"}
                              </Button>
                              <Button 
                                size="sm" 
                                variant="outline-secondary" 
                                disabled={submittingId !== null}
                                onClick={() => logWorkout(ex.exerciseId, "SKIPPED", 0)}
                              >
                                {submittingId === `${ex.exerciseId}-SKIPPED` ? "..." : "Skip"}
                              </Button>
                            </>
                          ) : (
                            <Button
                              size="sm"
                              variant="link"
                              className="text-muted p-0 text-decoration-none"
                              style={{ fontSize: "0.85rem" }}
                              onClick={() => logWorkout(ex.exerciseId, "ONGOING", 0)} // reset
                              disabled={submittingId !== null}
                            >
                              Hoàn tác
                            </Button>
                          )}
                        </div>
                      </div>
                    </Card>
                  </Col>
                );
              })}
            </Row>
          </>
        )}
      </Card.Body>
    </Card>
  );
}
