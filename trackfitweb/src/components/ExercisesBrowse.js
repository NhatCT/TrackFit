// src/components/ExercisesBrowse.js
import { useContext, useEffect, useMemo, useState } from "react";
import { Badge, Button, Card, Col, Form, Modal, Row } from "react-bootstrap";
import { Link, useNavigate } from "react-router-dom";
import { authApis, endpoints } from "../configs/Apis";
import MySpinner from "./layout/MySpinner";
import VideoPlayer from "./common/VideoPlayer";
import SimplePagination from "./common/SimplePagination";
import ExerciseCard from "./ExerciseCard";
import { toHttpUrl } from "../utils/youtubeUtils";
import { MyUserContext } from "../configs/Context";
import AOS from "aos";
import "aos/dist/aos.css";
import "./styles/Exercises.css";
import AddToPlanModal from "./AddToPlanModal";

const PAGE_SIZE = 12;

const ExercisesBrowse = () => {
  const [kw, setKw] = useState("");
  const [sort, setSort] = useState("newest"); // newest | name | muscle
  const [filters, setFilters] = useState({ muscle: "", goal: "" });

  const [page, setPage] = useState(1);
  const [res, setRes] = useState(null);
  const [loading, setLoading] = useState(false);

  const [preview, setPreview] = useState({ open: false, loading: false, data: null, error: "" });

  const [user] = useContext(MyUserContext);
  const nav = useNavigate();

  // xác định admin
  const isAdmin = !!(
    user?.role?.endsWith?.("ADMIN") ||
    user?.roles?.some?.((r) => (typeof r === "string" ? r.endsWith("ADMIN") : false)) ||
    user?.authorities?.some?.((a) =>
      (typeof a === "string" ? a : a?.authority)?.endsWith?.("ADMIN")
    )
  );

  // add-to-plan
  const [showAdd, setShowAdd] = useState(false);
  const [addItem, setAddItem] = useState(null);

  useEffect(() => {
    AOS.init({ duration: 700, once: true });
  }, []);

  const load = async () => {
    setLoading(true);
    try {
      const r = await authApis().get(endpoints.exercises, {
        params: { page, pageSize: PAGE_SIZE, kw: kw || undefined },
      });
      setRes(r.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page]);

  const openPreview = async (id) => {
    setPreview({ open: true, loading: true, data: null, error: "" });
    try {
      const r = await authApis().get(endpoints.exerciseDetail(id));
      setPreview({ open: true, loading: false, data: r.data, error: "" });
    } catch (e) {
      console.error(e);
      setPreview({ open: true, loading: false, data: null, error: "Không tải được dữ liệu bài tập." });
    }
  };

  const closePreview = () => setPreview({ open: false, loading: false, data: null, error: "" });

  // Lọc + sort phía client
  const itemsFilteredSorted = useMemo(() => {
    let items = res?.items || [];
    if (filters.muscle)
      items = items.filter((x) =>
        (x.muscleGroup || "").toLowerCase().includes(filters.muscle.toLowerCase())
      );
    if (filters.goal)
      items = items.filter((x) =>
        (x.targetGoal || "").toLowerCase().includes(filters.goal.toLowerCase())
      );

    const byNewest = (a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0);
    const byName = (a, b) => (a.name || "").localeCompare(b.name || "");
    const byMuscle = (a, b) => (a.muscleGroup || "").localeCompare(b.muscleGroup || "");

    if (sort === "name") return [...items].sort(byName);
    if (sort === "muscle") return [...items].sort(byMuscle);
    return [...items].sort(byNewest);
  }, [res, filters, sort]);

  // Tập choices filter
  const quickSets = useMemo(() => {
    const muscles = new Set();
    const goals = new Set();
    (res?.items || []).forEach((x) => {
      if (x.muscleGroup) muscles.add(x.muscleGroup);
      if (x.targetGoal) goals.add(x.targetGoal);
    });
    return { muscles: Array.from(muscles).sort(), goals: Array.from(goals).sort() };
  }, [res]);

  // Quản trị: Sửa/Xóa
  const onEdit = (id) => nav(`/exercises/${id}`);

  const onDelete = async (id) => {
    if (!window.confirm("Xóa bài tập này?")) return;
    try {
      await authApis().delete(endpoints.exerciseDetail(id));
      await load();
    } catch (e) {
      console.error(e);
      alert("Xóa thất bại. Vui lòng thử lại.");
    }
  };

  return (
    <>
      {/* Top bar */}
      <div className="d-flex justify-content-between align-items-center mb-3" data-aos="fade-right">
        <div className="d-flex align-items-center gap-2">
          <h3 className="m-0">Bài tập</h3>
          {res?.totalElements != null && <Badge bg="secondary">{res.totalElements}</Badge>}
        </div>

        {/* Nút Thêm: chỉ ADMIN */}
        <div className="d-flex align-items-center gap-2">
          {isAdmin && <Button as={Link} to="/exercises/new">Thêm</Button>}
        </div>
      </div>

      {/* Search + Filters */}
      <Card className="shadow-sm border-0 mb-3 hoverable" data-aos="fade-up">
        <Card.Body>
          <Form
            className="row g-2 align-items-end"
            onSubmit={(e) => {
              e.preventDefault();
              setPage(1);
              load();
            }}
          >
            <div className="col-12 col-md-4">
              <Form.Label className="small text-uppercase text-muted">Tìm kiếm</Form.Label>
              <Form.Control
                className="search-pill"
                placeholder="Từ khoá tên/mô tả..."
                value={kw}
                onChange={(e) => setKw(e.target.value)}
              />
            </div>

            <div className="col-6 col-md-3">
              <Form.Label className="small text-uppercase text-muted">Nhóm cơ</Form.Label>
              <Form.Select
                value={filters.muscle}
                onChange={(e) => setFilters((f) => ({ ...f, muscle: e.target.value }))}
              >
                <option value="">Tất cả</option>
                {quickSets.muscles.map((m) => (
                  <option key={m} value={m}>
                    {m}
                  </option>
                ))}
              </Form.Select>
            </div>

            <div className="col-6 col-md-3">
              <Form.Label className="small text-uppercase text-muted">Mục tiêu</Form.Label>
              <Form.Select
                value={filters.goal}
                onChange={(e) => setFilters((f) => ({ ...f, goal: e.target.value }))}
              >
                <option value="">Tất cả</option>
                {quickSets.goals.map((g) => (
                  <option key={g} value={g}>
                    {g}
                  </option>
                ))}
              </Form.Select>
            </div>

            <div className="col-6 col-md-2">
              <Form.Label className="small text-uppercase text-muted">Sắp xếp</Form.Label>
              <Form.Select value={sort} onChange={(e) => setSort(e.target.value)}>
                <option value="newest">Mới nhất</option>
                <option value="name">Tên (A→Z)</option>
                <option value="muscle">Nhóm cơ (A→Z)</option>
              </Form.Select>
            </div>

            <div className="col-6 col-md-12 d-flex gap-2 justify-content-end">
              <Button type="submit" variant="outline-primary">
                Áp dụng
              </Button>
              <Button
                variant="outline-secondary"
                onClick={() => {
                  setKw("");
                  setSort("newest");
                  setFilters({ muscle: "", goal: "" });
                  setPage(1);
                  load();
                }}
              >
                Xoá lọc
              </Button>
            </div>
          </Form>
        </Card.Body>
      </Card>

      {/* Grid */}
      {loading ? (
        <MySpinner />
      ) : (
        <>
          <Row xs={1} sm={2} md={3} lg={4} className="g-3" data-aos="fade-up">
            {itemsFilteredSorted?.length ? (
              itemsFilteredSorted.map((x) => (
                <Col key={x.exercisesId}>
                  <ExerciseCard
                    item={x}
                    onPreview={openPreview}

                    allowManage={isAdmin}
                    onEdit={onEdit}
                    onDelete={onDelete}
                  />
                </Col>
              ))
            ) : (
              <Col>
                <Card className="border-0 shadow-sm">
                  <Card.Body className="text-center text-muted">Không có dữ liệu</Card.Body>
                </Card>
              </Col>
            )}
          </Row>

          <SimplePagination
            page={page}
            totalPages={res?.totalPages}
            onPageChange={setPage}
            className="py-3"
          />
        </>
      )}

      {/* Modal xem video */}
      <Modal show={preview.open} onHide={closePreview} size="lg" centered>
        <Modal.Header closeButton>
          <Modal.Title>{preview.data?.name || "Xem bài tập"}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {preview.loading ? (
            <div className="text-center py-5">Đang tải...</div>
          ) : preview.error ? (
            <div className="text-danger">{preview.error}</div>
          ) : (
            <>
              {preview.data?.videoUrl ? (
                <>
                  <VideoPlayer url={preview.data.videoUrl} height="360px" />
                  <div className="mt-2">
                    <a href={toHttpUrl(preview.data.videoUrl)} target="_blank" rel="noreferrer">
                      Mở trên YouTube
                    </a>
                  </div>
                </>
              ) : (
                <div className="text-muted">Chưa có video cho bài tập này.</div>
              )}

              <div className="mt-3">
                <div>
                  <strong>Nhóm cơ:</strong> {preview.data?.muscleGroup || "—"}
                </div>
                <div>
                  <strong>Mục tiêu:</strong> {preview.data?.targetGoal || "—"}
                </div>
                <div>
                  <strong>Mô tả:</strong> {preview.data?.description || "—"}
                </div>
                <div className="text-muted small">
                  Tạo lúc: {preview.data?.createdAt ? new Date(preview.data.createdAt).toLocaleString() : "—"}
                </div>
              </div>
            </>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={closePreview}>Đóng</Button>
          {!!preview.data && (
            <Button
              variant="primary"
              onClick={() => {
                setAddItem({
                  exercisesId: preview.data.exercisesId,
                  name: preview.data.name,
                  muscleGroup: preview.data.muscleGroup,
                  videoUrl: preview.data.videoUrl,
                });
                setShowAdd(true);
              }}
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
        defaultDuration={30}
        onAdded={() => {}}
      />
    </>
  );
};

export default ExercisesBrowse;
