import { useEffect, useState, useMemo } from "react";
import { Card, Button, Badge, Row, Col, ProgressBar, Form, Alert, Modal } from "react-bootstrap";
import { authApis, endpoints } from "../configs/Apis";
import MySpinner from "./layout/MySpinner";
import ReactPlayer from "react-player";

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

  const [activeWorkoutOpen, setActiveWorkoutOpen] = useState(false);
  const [currentExerciseIndex, setCurrentExerciseIndex] = useState(0);
  const [secondsLeft, setSecondsLeft] = useState(0);
  const [timerRunning, setTimerRunning] = useState(false);
  const [isResting, setIsResting] = useState(false);
  const [showDetail, setShowDetail] = useState(false);

  // Active Workout Timer Effect
  useEffect(() => {
    let interval = null;
    if (timerRunning && secondsLeft > 0) {
      interval = setInterval(() => {
        setSecondsLeft((prev) => prev - 1);
      }, 1000);
    } else if (secondsLeft === 0 && timerRunning) {
      setTimerRunning(false);
      if (isResting) {
        handleRestFinished();
      }
    }
    return () => clearInterval(interval);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [timerRunning, secondsLeft, isResting]);

  const handleRestFinished = () => {
    setIsResting(false);
    const nextIdx = findNextUncompletedIndex(currentExerciseIndex);
    if (nextIdx !== -1) {
      setCurrentExerciseIndex(nextIdx);
      const nextEx = todayExercises[nextIdx];
      setSecondsLeft((nextEx.duration || 5) * 60);
      setTimerRunning(true);
    } else {
      // All completed!
      setCurrentExerciseIndex(-1);
    }
  };

  const findNextUncompletedIndex = (currentIdx) => {
    for (let i = currentIdx + 1; i < todayExercises.length; i++) {
      const ex = todayExercises[i];
      const recorded = historyToday.find((h) => h.exerciseId === ex.exerciseId);
      if (!recorded || (recorded.status !== "COMPLETED" && recorded.status !== "SKIPPED")) {
        return i;
      }
    }
    for (let i = 0; i <= currentIdx; i++) {
      const ex = todayExercises[i];
      const recorded = historyToday.find((h) => h.exerciseId === ex.exerciseId);
      if (!recorded || (recorded.status !== "COMPLETED" && recorded.status !== "SKIPPED")) {
        return i;
      }
    }
    return -1;
  };

  const handleActiveExerciseComplete = async (exerciseId, duration) => {
    await logWorkout(exerciseId, "COMPLETED", duration);
    const nextIdx = findNextUncompletedIndex(currentExerciseIndex);
    if (nextIdx !== -1) {
      setIsResting(true);
      setSecondsLeft(30); // 30s rest
      setTimerRunning(true);
    } else {
      setCurrentExerciseIndex(-1);
      setTimerRunning(false);
    }
  };

  const handleActiveExerciseSkip = async (exerciseId) => {
    await logWorkout(exerciseId, "SKIPPED", 0);
    const nextIdx = findNextUncompletedIndex(currentExerciseIndex);
    if (nextIdx !== -1) {
      setCurrentExerciseIndex(nextIdx);
      const nextEx = todayExercises[nextIdx];
      setSecondsLeft((nextEx.duration || 5) * 60);
      setTimerRunning(true);
    } else {
      setCurrentExerciseIndex(-1);
      setTimerRunning(false);
    }
  };

  const startActiveWorkout = () => {
    const firstUncompletedIdx = todayExercises.findIndex(ex => {
      const recorded = historyToday.find((h) => h.exerciseId === ex.exerciseId);
      return !recorded || (recorded.status !== "COMPLETED" && recorded.status !== "SKIPPED");
    });
    
    if (firstUncompletedIdx !== -1) {
      setCurrentExerciseIndex(firstUncompletedIdx);
      const ex = todayExercises[firstUncompletedIdx];
      setSecondsLeft((ex.duration || 5) * 60);
      setIsResting(false);
      setTimerRunning(true);
      setActiveWorkoutOpen(true);
    } else {
      setCurrentExerciseIndex(-1);
      setActiveWorkoutOpen(true);
    }
  };

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

  const totalMinutes = useMemo(
    () => todayExercises.reduce((s, e) => s + (Number(e.duration) || 0), 0),
    [todayExercises]
  );

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
      <Card className="shadow-sm border-0 mb-4" style={{ background: "var(--surface)", color: "var(--ink)" }}>
        <Card.Body className="text-center py-4">
          <MySpinner />
        </Card.Body>
      </Card>
    );
  }

  const currentEx = todayExercises[currentExerciseIndex];

  const formatTime = (secs) => {
    const m = Math.floor(secs / 60);
    const s = secs % 60;
    return `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
  };

  return (
    <>
      <Card className="shadow-sm border-0 mb-4" style={{ background: "var(--surface)", border: "1px solid var(--hair)", borderRadius: "16px" }} data-aos="fade-up">
        <Card.Header className="bg-transparent border-0 pt-4 pb-0 d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3">
          <div>
            <div className="d-flex align-items-center gap-2">
              <h4 className="fw-bold m-0" style={{ color: "var(--ink)" }}>🏋️ Kế hoạch hôm nay ({todayLabel})</h4>
              {todayExercises.length > 0 && stats.completed < stats.total && (
                <Button
                  size="sm"
                  variant="success"
                  onClick={startActiveWorkout}
                  className="fw-bold border-0 d-flex align-items-center gap-1 ms-2"
                  style={{ background: "var(--green)", borderRadius: "8px" }}
                >
                  ▶ Bắt đầu tập
                </Button>
              )}
            </div>
            <p className="text-muted m-0 small mt-1">Bắt đầu tập luyện và đánh dấu hoàn thành tiến độ của bạn</p>
          </div>

          {plans.length > 1 && (
            <div className="d-flex align-items-center gap-2">
              <span className="text-muted small text-nowrap">Chọn lịch:</span>
              <Form.Select
                size="sm"
                value={selectedPlanId}
                onChange={handlePlanChange}
                style={{ backgroundColor: "var(--surface-2)", color: "var(--ink)", borderColor: "var(--hair)", minWidth: "160px" }}
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
              <h5 className="fw-bold" style={{ color: "var(--ink)" }}>Chưa có kế hoạch tập luyện cá nhân</h5>
              <p className="text-muted small mb-3">Tạo một kế hoạch 7 ngày tùy chỉnh cho mục tiêu của riêng bạn để bắt đầu.</p>
              <Button variant="primary" size="sm" href="/plans/new">
                + Tạo kế hoạch ngay
              </Button>
            </div>
          ) : todayExercises.length === 0 ? (
            <div className="text-center py-4">
              <div className="fs-1 mb-2">😴</div>
              <h5 className="fw-bold" style={{ color: "var(--ink)" }}>Hôm nay là ngày nghỉ ngơi!</h5>
              <p className="text-muted small mb-0">Hãy nghỉ ngơi đầy đủ để cơ bắp được phục hồi hoặc tham khảo các bài tập nhẹ nhàng.</p>
            </div>
          ) : (
            <>
              {/* Summary tile — answers "hôm nay tôi làm gì?" in one glance */}
              <div className="d-flex align-items-center justify-content-between mb-2">
                <div>
                  <span
                    className="g-num fw-bold"
                    style={{ fontSize: "1.7rem", letterSpacing: "-.02em", color: stats.percent === 100 ? "var(--green)" : "var(--ink)" }}
                  >
                    {stats.completed}/{stats.total}
                  </span>
                  <span className="ms-2" style={{ color: "var(--muted)", fontSize: 13.5 }}>
                    bài hoàn thành{totalMinutes > 0 ? ` · ${totalMinutes} phút` : ""}
                  </span>
                </div>
                <button
                  onClick={() => setShowDetail((v) => !v)}
                  style={{ background: "none", border: "none", color: "var(--muted)", cursor: "pointer", fontSize: 13, fontWeight: 600, padding: "4px 0" }}
                  aria-expanded={showDetail}
                >
                  {showDetail ? "Thu gọn ↑" : "Chi tiết ↓"}
                </button>
              </div>

              <ProgressBar
                now={stats.percent}
                style={{ height: "6px", backgroundColor: "var(--hair)", borderRadius: 999, marginBottom: 0 }}
                variant={stats.percent === 100 ? "success" : "info"}
              />

              {/* Expandable exercise list */}
              {showDetail && (
                <Row className="g-3 mt-2">
                  {todayExercises.map((ex) => {
                    const recorded = historyToday.find((h) => h.exerciseId === ex.exerciseId);
                    const isCompleted = recorded?.status === "COMPLETED";
                    const isSkipped = recorded?.status === "SKIPPED";
                    return (
                      <Col key={ex.detailId} xs={12} md={6}>
                        <Card
                          className="h-100 border-0 p-3"
                          style={{
                            background: isCompleted
                              ? "color-mix(in srgb, var(--green) 8%, transparent)"
                              : isSkipped
                              ? "color-mix(in srgb, var(--muted) 10%, transparent)"
                              : "var(--surface-2)",
                            border: isCompleted
                              ? "1px solid color-mix(in srgb, var(--green) 30%, transparent)"
                              : "1px solid var(--hair)",
                            borderRadius: "12px",
                          }}
                        >
                          <div className="d-flex justify-content-between align-items-start gap-2">
                            <div>
                              <h6 className="fw-bold mb-1" style={{ color: "var(--ink)" }}>{ex.exerciseName}</h6>
                              <div className="d-flex gap-2 align-items-center flex-wrap mt-1">
                                <Badge bg="secondary" className="bg-opacity-25 text-light-50">⏱️ {ex.duration} phút</Badge>
                                {isCompleted && <Badge bg="success">Hoàn thành</Badge>}
                                {isSkipped && <Badge bg="secondary">Đã bỏ qua</Badge>}
                              </div>
                            </div>
                            <div className="d-flex gap-1">
                              {!isCompleted && !isSkipped ? (
                                <>
                                  <Button size="sm" variant="primary" disabled={submittingId !== null}
                                    onClick={() => logWorkout(ex.exerciseId, "COMPLETED", ex.duration)}>
                                    {submittingId === `${ex.exerciseId}-COMPLETED` ? "..." : "✓"}
                                  </Button>
                                  <Button size="sm" variant="outline-secondary" disabled={submittingId !== null}
                                    onClick={() => logWorkout(ex.exerciseId, "SKIPPED", 0)}>
                                    {submittingId === `${ex.exerciseId}-SKIPPED` ? "..." : "Skip"}
                                  </Button>
                                </>
                              ) : (
                                <Button size="sm" variant="link" className="text-muted p-0 text-decoration-none"
                                  style={{ fontSize: "0.85rem" }} disabled={submittingId !== null}
                                  onClick={() => logWorkout(ex.exerciseId, "ONGOING", 0)}>
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
              )}
            </>
          )}
        </Card.Body>
      </Card>

      {/* Active Workout Modal */}
      <Modal
        show={activeWorkoutOpen}
        onHide={() => {
          setActiveWorkoutOpen(false);
          setTimerRunning(false);
        }}
        size="lg"
        centered
        backdrop="static"
        keyboard={false}
        contentClassName="bg-dark text-light border-secondary"
      >
        <Modal.Header closeButton className="border-secondary text-light bg-dark">
          <Modal.Title className="fw-bold">⏱️ Chế độ tập luyện tương tác</Modal.Title>
        </Modal.Header>

        {currentExerciseIndex === -1 ? (
          <Modal.Body className="text-center py-5 bg-dark">
            <div style={{ fontSize: "4.5rem" }}>🏆🎉</div>
            <h3 className="fw-bold text-success mt-3">Hoàn thành buổi tập!</h3>
            <p className="text-muted mt-2 mx-auto" style={{ maxWidth: "480px" }}>
              Tuyệt vời! Bạn đã hoàn thành tất cả các mục tiêu tập luyện hôm nay. Hãy nghỉ ngơi đầy đủ để phục hồi cơ bắp!
            </p>
            <Button variant="primary" className="mt-3 px-5 py-2 fw-bold" onClick={() => setActiveWorkoutOpen(false)}>
              Hoàn tất
            </Button>
          </Modal.Body>
        ) : isResting ? (
          <Modal.Body className="text-center py-5 bg-dark" style={{ background: "rgba(37,99,235,0.03)" }}>
            <div style={{ fontSize: "3.5rem" }}>🍉🥤</div>
            <h3 className="fw-bold text-info mt-3">Thời gian nghỉ ngơi</h3>
            <div className="fs-1 fw-bold text-light my-3" style={{ letterSpacing: "2px", fontFamily: "monospace" }}>
              {formatTime(secondsLeft)}
            </div>
            <p className="text-muted mb-2">Hít thở đều và uống một chút nước nhé!</p>
            <p className="text-muted small">
              Bài tiếp theo: <strong>{todayExercises[findNextUncompletedIndex(currentExerciseIndex)]?.exerciseName}</strong>
            </p>
            <Button 
              variant="outline-info" 
              className="mt-3 px-4 fw-bold"
              onClick={() => handleRestFinished()}
            >
              Bỏ qua nghỉ ngơi (Skip Rest)
            </Button>
          </Modal.Body>
        ) : (
          currentEx && (
            <Modal.Body className="py-4 bg-dark">
              <div className="d-flex justify-content-between align-items-center mb-3">
                <Badge bg="primary" style={{ fontSize: "0.85rem", padding: "6px 12px" }}>
                  Bài {currentExerciseIndex + 1} / {todayExercises.length}
                </Badge>
                <Badge bg="secondary" style={{ fontSize: "0.85rem", padding: "6px 12px" }}>
                  ⏱️ {currentEx.duration} phút
                </Badge>
              </div>
              <h3 className="fw-bold text-light mb-3 text-center">{currentEx.exerciseName}</h3>
              
              {/* Video Player */}
              {currentEx.videoUrl ? (
                <div className="mb-4 rounded-3 overflow-hidden bg-black" style={{ height: "320px", border: "1px solid rgba(255,255,255,0.08)" }}>
                  <ReactPlayer 
                    url={currentEx.videoUrl} 
                    width="100%" 
                    height="100%" 
                    controls 
                  />
                </div>
              ) : (
                <div className="text-center py-5 mb-4 bg-secondary bg-opacity-10 rounded-3 border border-secondary border-dashed" style={{ borderColor: "rgba(255,255,255,0.15) !important" }}>
                  <div style={{ fontSize: "3.5rem" }}>💪⚡</div>
                  <p className="text-muted small mt-2">Bắt đầu bài tập này ngay trên thảm tập của bạn!</p>
                </div>
              )}
              
              {/* Timer widget */}
              <div className="text-center my-4">
                <div className="fw-bold text-light mb-3" style={{ fontSize: "3rem", fontFamily: "monospace", letterSpacing: "1px" }}>
                  {formatTime(secondsLeft)}
                </div>
                <div className="d-flex justify-content-center gap-2">
                  <Button 
                    size="sm" 
                    variant={timerRunning ? "warning" : "success"}
                    onClick={() => setTimerRunning(!timerRunning)}
                    className="px-3 py-2 fw-semibold"
                  >
                    {timerRunning ? "⏸ Tạm dừng" : "▶ Tiếp tục"}
                  </Button>
                  <Button 
                    size="sm" 
                    variant="outline-secondary"
                    onClick={() => setSecondsLeft((currentEx.duration || 5) * 60)}
                    className="px-3 py-2"
                  >
                    🔄 Đặt lại
                  </Button>
                </div>
              </div>
              
              <div className="d-flex gap-2 justify-content-center mt-4">
                <Button 
                  variant="success" 
                  className="fw-bold px-4 py-2"
                  style={{ flex: 2, background: "var(--green)", border: "none" }}
                  onClick={() => handleActiveExerciseComplete(currentEx.exerciseId, currentEx.duration)}
                >
                  ✓ Hoàn thành
                </Button>
                <Button 
                  variant="outline-secondary" 
                  className="px-4 py-2"
                  style={{ flex: 1 }}
                  onClick={() => handleActiveExerciseSkip(currentEx.exerciseId)}
                >
                  Bỏ qua
                </Button>
              </div>
            </Modal.Body>
          )
        )}
      </Modal>
    </>
  );
}
