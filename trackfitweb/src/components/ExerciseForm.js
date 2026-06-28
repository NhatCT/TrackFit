// src/components/ExerciseForm.jsx
import { useEffect, useMemo, useState, useContext } from "react";
import { Alert, Button, Card, Col, Form, Row } from "react-bootstrap";
import { Link, useNavigate, useParams } from "react-router-dom";
import { authApis, endpoints } from "../configs/Apis";
import MySpinner from "./layout/MySpinner";
import { MyUserContext } from "../configs/Context";

const ExerciseForm = () => {
  const { id } = useParams();               // có id => edit, không có => create
  const isEdit = useMemo(() => !!id, [id]);
  const nav = useNavigate();

  const [user] = useContext(MyUserContext);
  const isAdmin = !!(
    user?.role?.endsWith?.("ADMIN") ||
    user?.roles?.includes?.("ADMIN") ||
    user?.authorities?.some?.((a) =>
      (typeof a === "string" ? a : a?.authority)?.endsWith?.("ADMIN")
    )
  );

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [msg, setMsg] = useState("");

  const [form, setForm] = useState({
    name: "",
    muscleGroup: "",
    targetGoal: "",
    description: "",
    videoUrl: "",
  });

  // load chi tiết khi edit
  useEffect(() => {
    const load = async () => {
      if (!isEdit) return;
      setLoading(true);
      setMsg("");
      try {
        const r = await authApis().get(endpoints.exerciseDetail(id));
        const d = r.data || {};
        setForm({
          name: d.name ?? "",
          muscleGroup: d.muscleGroup ?? "",
          targetGoal: d.targetGoal ?? "",
          description: d.description ?? "",
          videoUrl: d.videoUrl ?? "",
        });
      } catch (e) {
        console.error(e);
        setMsg("Không tải được dữ liệu bài tập.");
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [id, isEdit]);

  const onChange = (field) => (e) =>
    setForm((prev) => ({ ...prev, [field]: e.target.value }));

  const validate = () => {
    if (!form.name.trim()) return "Tên bài tập không được trống";
    // Có thể bổ sung validate khác ở đây
    return "";
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setMsg("");

    const err = validate();
    if (err) {
      setMsg(err);
      return;
    }

    setSaving(true);
    try {
      if (isEdit) {
        await authApis().put(endpoints.exerciseDetail(id), form);
      } else {
        await authApis().post(endpoints.exercises, form);
      }
      nav("/exercises");
    } catch (ex) {
      console.error(ex);
      const m =
        ex.response?.data?.message ||
        (ex.response?.status === 403
          ? "Bạn không có quyền thực hiện thao tác này."
          : "Lưu thất bại! Vui lòng thử lại.");
      setMsg(m);
    } finally {
      setSaving(false);
    }
  };

  const onDelete = async () => {
    if (!isEdit) return;
    if (!window.confirm("Xóa bài tập này?")) return;
    setSaving(true);
    try {
      await authApis().delete(endpoints.exerciseDetail(id));
      nav("/exercises");
    } catch (ex) {
      console.error(ex);
      const m =
        ex.response?.data?.message ||
        (ex.response?.status === 403
          ? "Bạn không có quyền xóa bài tập."
          : "Xóa thất bại! Vui lòng thử lại.");
      setMsg(m);
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <MySpinner />;

  return (
    <Card className="shadow-sm border-0">
      <Card.Body>
        <div className="d-flex justify-content-between align-items-center mb-3">
          <h4 className="m-0">{isEdit ? "Sửa bài tập" : "Thêm bài tập"}</h4>
          <div className="d-flex gap-2">
            <Button as={Link} to="/exercises" variant="outline-secondary">
              Quay lại
            </Button>
            {/* Chỉ hiện Xóa khi đang sửa và FE có admin (BE vẫn kiểm tra role) */}
            {isEdit && isAdmin && (
              <Button variant="outline-danger" onClick={onDelete} disabled={saving}>
                Xóa
              </Button>
            )}
          </div>
        </div>

        {msg && <Alert variant="danger" className="mb-3">{msg}</Alert>}

        <Form onSubmit={onSubmit}>
          <Row className="g-3">
            <Col md={6}>
              <Form.Group>
                <Form.Label>Tên bài tập</Form.Label>
                <Form.Control
                  placeholder="Ví dụ: Push-up"
                  value={form.name}
                  onChange={onChange("name")}
                  required
                />
              </Form.Group>
            </Col>

            <Col md={6}>
              <Form.Group>
                <Form.Label>Nhóm cơ</Form.Label>
                <Form.Control
                  placeholder="Ví dụ: Chest"
                  value={form.muscleGroup}
                  onChange={onChange("muscleGroup")}
                />
              </Form.Group>
            </Col>

            <Col md={6}>
              <Form.Group>
                <Form.Label>Mục tiêu</Form.Label>
                <Form.Control
                  placeholder="Ví dụ: Strength"
                  value={form.targetGoal}
                  onChange={onChange("targetGoal")}
                />
              </Form.Group>
            </Col>

            <Col md={12}>
              <Form.Group>
                <Form.Label>Mô tả</Form.Label>
                <Form.Control
                  as="textarea"
                  rows={4}
                  placeholder="Mô tả ngắn gọn cách thực hiện, lưu ý…"
                  value={form.description}
                  onChange={onChange("description")}
                />
              </Form.Group>
            </Col>

            <Col md={12}>
              <Form.Group>
                <Form.Label>Video URL (YouTube)</Form.Label>
                <Form.Control
                  placeholder="https://www.youtube.com/watch?v=xxxxxxx"
                  value={form.videoUrl}
                  onChange={onChange("videoUrl")}
                />
                <Form.Text className="text-muted">
                  Hỗ trợ link dạng `v=`, `youtu.be/`, hoặc `/embed/`.
                </Form.Text>
              </Form.Group>
            </Col>
          </Row>

          <div className="d-flex justify-content-end gap-2 mt-4">
            <Button as={Link} to="/exercises" variant="outline-secondary">
              Hủy
            </Button>
            <Button type="submit" variant="primary" disabled={saving}>
              {saving ? "Đang lưu..." : isEdit ? "Cập nhật" : "Tạo mới"}
            </Button>
          </div>
        </Form>
      </Card.Body>
    </Card>
  );
};

export default ExerciseForm;
