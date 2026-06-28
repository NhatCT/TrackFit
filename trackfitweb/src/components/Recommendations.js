// src/components/Recommendations.jsx
import { useCallback, useEffect, useMemo, useState } from "react";
import { Button, Card, Row, Col, Badge, Spinner, Alert, Modal } from "react-bootstrap";
import { authApis, endpoints } from "../configs/Apis";
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
  }, []);

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
          <h3 className="mb-1">Gợi ý bài tập (AI Agent)</h3>
          <div className="text-muted small">
            Danh sách dưới đây được AI tạo tự động dựa trên hồ sơ và lịch sử của bạn.
          </div>
        </div>
        <Button variant="outline-primary" onClick={loadAuto} disabled={loading}>
          {loading ? <Spinner size="sm" /> : "Làm mới gợi ý"}
        </Button>
      </div>

      {err && <Alert variant="danger">{err}</Alert>}

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
