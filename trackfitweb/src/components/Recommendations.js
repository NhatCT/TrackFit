// src/components/Recommendations.jsx
import { useCallback, useEffect, useMemo, useState, useContext } from "react";
import { Button, Card, Row, Col, Badge, Spinner, Alert, Modal } from "react-bootstrap";
import { authApis, endpoints } from "../configs/Apis";
import { MyUserContext } from "../configs/Context";
import { getKey, formatScore } from "../utils/recoUtils";
import ExerciseCard from "./ExerciseCard";
import VideoPlayer from "./common/VideoPlayer";
import AddToPlanModal from "./AddToPlanModal";

/** ================= Utils ================= **/
const toHttps = (u) => (!u ? "" : /^https?:\/\//i.test(u) ? u : `https://${u.replace(/^\/\//, "")}`);
const ytId = (u) => {
  if (!u) return null;
  const url = toHttps(u);
  const m1 = url.match(/[?&]v=([A-Za-z0-9_-]{11})/);
  if (m1) return m1[1];
  const m2 = url.match(/youtu\.be\/([A-Za-z0-9_-]{11})/);
  if (m2) return m2[1];
  const m3 = url.match(/youtube\.com\/embed\/([A-Za-z0-9_-]{11})/);
  if (m3) return m3[1];
  return null;
};
const getYoutubeThumbnail = (url) => {
  const id = ytId(url);
  return id ? `https://i.ytimg.com/vi/${id}/hqdefault.jpg` : null;
};

/** ================ Component ================ **/
const Recommendations = () => {
  const [user] = useContext(MyUserContext);
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState("");

  // preview modal
  const [showPreview, setShowPreview] = useState(false);
  const [previewItem, setPreviewItem] = useState(null);

  // add-to-plan modal
  const [showAdd, setShowAdd] = useState(false);
  const [addItem, setAddItem] = useState(null);

  const loadAuto = useCallback(async () => {
    if (!user?.isPremium) {
      // Mock items for premium teaser blur
      const mockItems = [
        { id: 1, name: "Full Body Blast HIIT", estimatedMinutes: 20, score: 0.96, muscleGroup: "Toàn thân", description: "Bài tập đốt calo cường độ cao." },
        { id: 2, name: "Upper Body Strength PRO", estimatedMinutes: 35, score: 0.92, muscleGroup: "Ngực/Vai", description: "Tập trung xây dựng cơ bắp thân trên." },
        { id: 3, name: "Core Definition Extreme", estimatedMinutes: 15, score: 0.89, muscleGroup: "Bụng", description: "Điêu khắc cơ bụng 6 múi săn chắc." },
        { id: 4, name: "Leg Day Hypertrophy", estimatedMinutes: 45, score: 0.88, muscleGroup: "Đùi/Mông", description: "Kích thích phát triển nhóm cơ đùi sau." }
      ];
      setItems(mockItems);
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setErr("");
      const r = await authApis().get(endpoints.recommendationsAuto, {
        params: { size: 8 },
      });
      const data = Array.isArray(r.data) ? r.data : [];
      const sorted = [...data].sort((a, b) => (b.score ?? 0) - (a.score ?? 0));
      setItems(sorted);
    } catch (e) {
      const status = e?.response?.status;
      setErr(
        status === 401
          ? "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
          : "Không tải được gợi ý tự động."
      );
    } finally {
      setLoading(false);
    }
  }, [user?.isPremium]);

  useEffect(() => {
    loadAuto();
  }, [loadAuto]);

  const exerciseLikeItems = useMemo(() => {
    return items.map((it, idx) => {
      const key = getKey(it) ?? idx;
      const minutes = it.estimatedMinutes ?? null;
      const score = it.score != null ? formatScore(it.score) : null;

      const targetPieces = [];
      if (minutes != null) targetPieces.push(`${minutes}’`);
      if (score != null) targetPieces.push(`Độ phù hợp ${score}`);

      return {
        exercisesId: it.exercisesId ?? it.exerciseId ?? it.id ?? key,
        name: it.name,
        description: (it.description && it.description.trim()) ? it.description : (it.reason || "—"),
        muscleGroup: it.muscleGroup || it.primaryMuscle || null,
        targetGoal: targetPieces.length ? targetPieces.join(" • ") : (it.targetGoal ?? null),
        createdAt: it.createdAt || it.generatedAt || null,
        videoUrl: it.videoUrl || it.demoUrl || null,
        __raw: it,
      };
    });
  }, [items]);

  const onPreview = (id) => {
    const found =
      exerciseLikeItems.find((x) => String(x.exercisesId) === String(id)) ||
      exerciseLikeItems.find((x) => String(getKey(x.__raw)) === String(id));
    if (found) {
      setPreviewItem(found);
      setShowPreview(true);
    }
  };

  return (
    <>
      <div className="d-flex justify-content-between align-items-end mb-3">
        <div>
          <h3 className="mb-1">Gợi ý bài tập dành cho bạn</h3>
          <div className="text-muted small">
            Được phân tích dựa trên hồ sơ và lịch sử tập luyện của bạn.
          </div>
        </div>
        {user?.isPremium && (
          <Button variant="outline-primary" onClick={loadAuto} disabled={loading}>
            {loading ? <Spinner size="sm" /> : "Làm mới gợi ý"}
          </Button>
        )}
      </div>

      {err && <Alert variant="danger">{err}</Alert>}

      <div style={{ position: "relative" }}>
        {!user?.isPremium && (
          <div 
            style={{
              position: "absolute",
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              background: "rgba(11, 18, 32, 0.8)",
              backdropFilter: "blur(6px)",
              zIndex: 10,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              borderRadius: "16px",
              border: "1px solid rgba(255,255,255,0.06)",
              minHeight: "320px"
            }}
            className="p-4 text-center"
          >
            <div style={{ maxWidth: "480px" }}>
              <div className="fs-1 mb-3">👑</div>
              <h4 className="text-white mb-3">Tính Năng Dành Riêng Cho Hội Viên PRO</h4>
              <p className="text-light-50 small mb-4">
                Chức năng gợi ý bài tập thông minh được tối ưu hóa và phân tích riêng cho thể trạng của bạn chỉ dành cho tài khoản PRO.
              </p>
              <Button href="/upgrade" variant="warning" className="fw-bold text-dark px-4 py-2.5">
                Nâng Cấp GUTIM PRO Ngay
              </Button>
            </div>
          </div>
        )}

        <div style={{ filter: !user?.isPremium ? "blur(6px)" : "none", pointerEvents: !user?.isPremium ? "none" : "auto" }}>
          {loading ? (
            <div className="d-flex justify-content-center py-5">
              <Spinner animation="border" />
            </div>
          ) : exerciseLikeItems.length ? (
            <Row xs={1} sm={2} lg={3} xl={4} className="g-3">
              {exerciseLikeItems.map((it) => (
                <Col key={it.exercisesId}>
                  <ExerciseCard
                    item={it}
                    onPreview={onPreview}
                    getYoutubeThumbnail={getYoutubeThumbnail}
                    allowManage={false}
                  />
                </Col>
              ))}
            </Row>
          ) : (
            <Card className="shadow-sm border-0">
              <Card.Body className="text-center text-muted">Chưa có gợi ý</Card.Body>
            </Card>
          )}
        </div>
      </div>

      {/* Preview Modal */}
      <Modal show={showPreview} onHide={() => setShowPreview(false)} size="lg" centered>
        <Modal.Header closeButton>
          <Modal.Title>{previewItem?.name || "Xem nhanh"}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {previewItem?.videoUrl ? (
            <VideoPlayer url={previewItem.videoUrl} height="420px" />
          ) : (
            <div className="text-muted">Chưa có video minh họa</div>
          )}

          <div className="mt-3">
            <div className="d-flex flex-wrap gap-2 mb-2">
              {previewItem?.muscleGroup && (
                <Badge bg="dark" className="text-uppercase">
                  {previewItem.muscleGroup}
                </Badge>
              )}
              {previewItem?.targetGoal && <Badge bg="secondary">{previewItem.targetGoal}</Badge>}
            </div>
            <p className="mb-0">{previewItem?.description}</p>
          </div>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowPreview(false)}>
            Đóng
          </Button>
          {!!previewItem && (
            <Button
              variant="primary"
              onClick={() => { setAddItem(previewItem); setShowAdd(true); }}
            >
              Thêm vào kế hoạch
            </Button>
          )}
        </Modal.Footer>
      </Modal>

      {/* Modal AddToPlan */}
      <AddToPlanModal
        show={showAdd}
        onHide={() => setShowAdd(false)}
        exercise={addItem}
        defaultDuration={addItem?.__raw?.estimatedMinutes || 30}
        onAdded={() => {}}
      />
    </>
  );
};

export default Recommendations;
