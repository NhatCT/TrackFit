import { useEffect, useState } from "react";
import { Button, Form, Table, Card, Badge } from "react-bootstrap";
import { Link } from "react-router-dom";
import { authApis, endpoints } from "../configs/Apis";
import MySpinner from "./layout/MySpinner";
import SimplePagination from "./common/SimplePagination";
import "./styles/Plans.css";

const PlansList = () => {
  const [kw, setKw] = useState("");
  const [page, setPage] = useState(1);
  const [res, setRes] = useState(null);
  const [loading, setLoading] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const r = await authApis().get(endpoints.plans, {
        params: { page, pageSize: 10, kw: kw || undefined },
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

  const del = async (id) => {
    if (!window.confirm("Xóa kế hoạch này?")) return;
    await authApis().delete(endpoints.planDetail(id));
    load();
  };

  return (
    <>
      <div className="d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-3 mb-3">
        <div className="d-flex align-items-center gap-2">
          <h3 className="m-0">Kế hoạch của bạn</h3>
          {res?.totalElements != null && <Badge bg="secondary">{res.totalElements}</Badge>}
        </div>
        <div className="d-flex align-items-center gap-2">
          <Button as={Link} to="/plans/new">+ Tạo kế hoạch</Button>
        </div>
      </div>

      <Card className="shadow-sm border-0 mb-3 hoverable">
        <Card.Body>
          <Form
            className="d-flex flex-column flex-md-row gap-2"
            onSubmit={(e) => {
              e.preventDefault();
              setPage(1);
              load();
            }}
          >
            <Form.Control
              className="search-pill"
              placeholder="Tìm theo tên kế hoạch..."
              value={kw}
              onChange={(e) => setKw(e.target.value)}
            />
            <div className="d-flex gap-2">
              <Button variant="outline-primary" type="submit">Tìm</Button>
              <Button
                variant="outline-secondary"
                type="button"
                onClick={() => { setKw(""); setPage(1); load(); }}
              >
                Xoá lọc
              </Button>
            </div>
          </Form>
        </Card.Body>
      </Card>

      {loading ? (
        <MySpinner/>
      ) : (
        <>
          <Card className="shadow-sm border-0">
            <Table hover responsive className="m-0 align-middle">
              <thead className="table-light">
                <tr>
                  <th style={{width: 80}}>ID</th>
                  <th>Tên kế hoạch</th>
                  <th style={{width:120}}>Loại</th>
                  <th style={{width:210}}>Tạo lúc</th>
                  <th style={{width:220}} className="text-end">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {res?.items?.length ? (
                  res.items.map((p) => (
                    <tr key={p.planId}>
                      <td>#{p.planId}</td>
                      <td className="fw-semibold">{p.planName}</td>
                      <td>{p.isTemplate ? <Badge bg="dark">Template</Badge> : <Badge bg="info">Cá nhân</Badge>}</td>
                      <td>{new Date(p.createdAt).toLocaleString()}</td>
                      <td className="text-end">
                        <Button as={Link} size="sm" variant="outline-primary" to={`/plans/${p.planId}`}>
                          Xem / Sửa
                        </Button>{" "}
                        <Button size="sm" variant="outline-danger" onClick={() => del(p.planId)}>
                          Xóa
                        </Button>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={5} className="text-center text-muted py-5">
                      Chưa có kế hoạch. Hãy nhấn <b>“+ Tạo kế hoạch”</b> để bắt đầu.
                    </td>
                  </tr>
                )}
              </tbody>
            </Table>
          </Card>

          <SimplePagination
            page={page}
            totalPages={res?.totalPages}
            onPageChange={setPage}
          />
        </>
      )}
    </>
  );
};

export default PlansList;
