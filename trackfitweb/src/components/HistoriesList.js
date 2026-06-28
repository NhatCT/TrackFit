// src/components/HistoriesList.js
import { useEffect, useState } from "react";
import { Button, Form, Table, Card } from "react-bootstrap";
import { authApis, endpoints } from "../configs/Apis";
import MySpinner from "./layout/MySpinner";

const HistoriesList = () => {
  const [res, setRes] = useState(null);
  const [loading, setLoading] = useState(false);

  // ---- filters ----
  const [filters, setFilters] = useState({
    page: 1,
    pageSize: 10,
    planId: "",
    exerciseId: "",
    status: ""
  });

  // ---- dropdown data ----
  const [planOptions, setPlanOptions] = useState([]);
  const [exerciseOptions, setExerciseOptions] = useState([]);
  const [kwPlan, setKwPlan] = useState("");
  const [kwEx, setKwEx] = useState("");

  const load = async () => {
    setLoading(true);
    try {
      const r = await authApis().get(endpoints.histories, {
        params: {
          page: filters.page,
          pageSize: filters.pageSize,
          planId: filters.planId || undefined,
          exerciseId: filters.exerciseId || undefined,
          status: filters.status || undefined
        }
      });
      setRes(r.data);
    } finally {
      setLoading(false);
    }
  };

  // load table khi đổi trang
  useEffect(() => {
    load();
    // eslint-disable-next-line
  }, [filters.page]);

  // load options cho dropdown (plans + exercises)
  useEffect(() => {
    (async () => {
      try {
        // Plans của user hiện tại
        const rp = await authApis().get(endpoints.plans, {
          params: { page: 1, pageSize: 100 }
        });
        const planItems = rp.data?.items || [];
        setPlanOptions(
          planItems.map((p) => ({
            id: p.planId,
            label: `${p.planName} (#${p.planId})`
          }))
        );

        // Exercises
        const re = await authApis().get(endpoints.exercises, {
          params: { page: 1, pageSize: 100 }
        });
        const exItems = re.data?.items || [];
        setExerciseOptions(
          exItems.map((x) => ({
            id: x.exercisesId,
            label: `${x.name} (#${x.exercisesId})`
          }))
        );
      } catch {
        // ignore
      }
    })();
  }, []);

  // helper: render badge trạng thái
  const renderStatus = (s) => {
    if (s === "COMPLETED")
      return <span className="badge bg-success">COMPLETED</span>;
    if (s === "ONGOING")
      return <span className="badge bg-primary">ONGOING</span>;
    if (s === "SKIPPED")
      return <span className="badge bg-warning text-dark">SKIPPED</span>;
    if (s === "MISSED") return <span className="badge bg-danger">MISSED</span>;
    return s || "—";
  };

  return (
    <>
      <h3 className="mb-3">Lịch sử tập luyện</h3>

      {/* Bộ lọc */}
      <Card className="shadow-sm border-0 mb-3">
        <Card.Body>
          <Form
            className="row g-2"
            onSubmit={(e) => {
              e.preventDefault();
              setFilters((f) => ({ ...f, page: 1 }));
              load();
            }}
          >
            {/* Plan dropdown có ô tìm nhanh */}
            <div className="col-md-3">
              <Form.Control
                placeholder="Tìm kế hoạch..."
                className="mb-1"
                value={kwPlan}
                onChange={(e) => setKwPlan(e.target.value)}
              />
              <Form.Select
                value={filters.planId}
                onChange={(e) =>
                  setFilters({ ...filters, planId: e.target.value })
                }
              >
                <option value="">-- Tất cả kế hoạch --</option>
                {planOptions
                  .filter(
                    (p) =>
                      !kwPlan ||
                      p.label.toLowerCase().includes(kwPlan.toLowerCase())
                  )
                  .map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.label}
                    </option>
                  ))}
              </Form.Select>
            </div>

            {/* Exercise dropdown có ô tìm nhanh */}
            <div className="col-md-3">
              <Form.Control
                placeholder="Tìm bài tập..."
                className="mb-1"
                value={kwEx}
                onChange={(e) => setKwEx(e.target.value)}
              />
              <Form.Select
                value={filters.exerciseId}
                onChange={(e) =>
                  setFilters({ ...filters, exerciseId: e.target.value })
                }
              >
                <option value="">-- Tất cả bài tập --</option>
                {exerciseOptions
                  .filter(
                    (x) =>
                      !kwEx ||
                      x.label.toLowerCase().includes(kwEx.toLowerCase())
                  )
                  .map((x) => (
                    <option key={x.id} value={x.id}>
                      {x.label}
                    </option>
                  ))}
              </Form.Select>
            </div>

            {/* Status */}
            <div className="col-md-3">
              <Form.Select
                value={filters.status}
                onChange={(e) =>
                  setFilters({ ...filters, status: e.target.value })
                }
              >
                <option value="">-- Trạng thái --</option>
                <option value="COMPLETED">COMPLETED</option>
                <option value="SKIPPED">SKIPPED</option>
                <option value="MISSED">MISSED</option>
                <option value="ONGOING">ONGOING</option>
              </Form.Select>
            </div>

            <div className="col-md-3">
              <Button type="submit" className="w-100">
                Lọc
              </Button>
            </div>
          </Form>
        </Card.Body>
      </Card>

      {/* Bảng dữ liệu */}
      {loading ? (
        <MySpinner />
      ) : (
        <>
          <Card className="shadow-sm border-0">
            <Table hover responsive className="m-0 align-middle">
              <thead className="table-light">
                <tr>
                  <th style={{ width: 90 }}>ID</th>
                  <th style={{ width: 220 }}>Ngày hoàn thành</th>
                  <th>Plan</th>
                  <th>Exercise</th>
                  <th style={{ width: 110, textAlign: "right" }}>Phút</th>
                  <th style={{ width: 140 }}>Trạng thái</th>
                </tr>
              </thead>
              <tbody>
                {res?.items?.length ? (
                  res.items.map((h) => (
                    <tr key={h.historyId}>
                      <td>#{h.historyId}</td>
                      <td>
                        {h.completedAt
                          ? new Date(h.completedAt).toLocaleString()
                          : "—"}
                      </td>
                      <td>
                        {h.planName} (#{h.planId})
                      </td>
                      <td>
                        {h.exerciseName} (#{h.exerciseId})
                      </td>
                      <td style={{ textAlign: "right" }}>
                        {h.duration != null && h.duration > 0
                          ? `${h.duration} phút`
                          : "—"}
                      </td>
                      <td>{renderStatus(h.status)}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={6} className="text-center text-muted">
                      Không có dữ liệu
                    </td>
                  </tr>
                )}
              </tbody>
            </Table>
          </Card>

          {/* Phân trang */}
          {res && res.totalPages > 1 && (
            <div className="d-flex gap-2 mt-3">
              <Button
                disabled={filters.page <= 1}
                onClick={() =>
                  setFilters((f) => ({ ...f, page: f.page - 1 }))
                }
              >
                Trước
              </Button>
              <div className="align-self-center">
                Trang {filters.page}/{res.totalPages}
              </div>
              <Button
                disabled={filters.page >= res.totalPages}
                onClick={() =>
                  setFilters((f) => ({ ...f, page: f.page + 1 }))
                }
              >
                Sau
              </Button>
            </div>
          )}
        </>
      )}
    </>
  );
};

export default HistoriesList;
