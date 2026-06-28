
import { useContext, useEffect, useState } from "react";
import {
  Badge,
  Button,
  Card,
  Form,
  Table,
  Pagination,
  Row,
  Col,
  InputGroup,
} from "react-bootstrap";
import { authApis, endpoints } from "../configs/Apis";
import { MyUserContext } from "../configs/Context";
import MySpinner from "./layout/MySpinner";
import AiAdviceModal from "./AiAdviceModal"; 

const TYPE_OPTIONS = [
  { value: "", label: "Tất cả loại" },
  { value: "ADVICE", label: "Lời khuyên (ADVICE)" },
  { value: "REMINDER", label: "Nhắc nhở (REMINDER)" },
  { value: "SYSTEM", label: "Hệ thống (SYSTEM)" },
];

// Chuẩn hóa dữ liệu trả về: có thể là mảng hoặc object {items, page,...}
const normalizeItems = (data) => {
  const arr = Array.isArray(data) ? data : data?.items || [];
  const meta = {
    page: Array.isArray(data) ? 1 : data?.page ?? 1,
    pageSize: Array.isArray(data) ? arr.length : data?.pageSize ?? arr.length,
    totalPages: Array.isArray(data) ? 1 : data?.totalPages ?? 1,
    totalElements: Array.isArray(data) ? arr.length : data?.totalElements ?? arr.length,
  };
  return { arr, meta };
};

const fmtTime = (v) => {
  try {
    const d = new Date(v);
    if (Number.isNaN(d.getTime())) return "";
    return d.toLocaleString();
  } catch {
    return "";
  }
};

const isAdminRole = (role) => {
  if (!role) return false;
  const r = String(role).toUpperCase();
  return r.includes("ADMIN");
};

const NotificationsList = () => {
  // Lấy user từ context để biết có phải ADMIN hay không (hiện nút admin)
  const [userCtx] = useContext(MyUserContext);
  const isAdmin = isAdminRole(userCtx?.role);

  // danh sách
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  // filter
  const [kw, setKw] = useState("");
  const [isRead, setIsRead] = useState(undefined);
  const [type, setType] = useState("");

  // paging
  const [page, setPage] = useState(1);
  const [pageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // unread badge
  const [unread, setUnread] = useState(0);

  // số ngày dọn cũ
  const [cleanupDays, setCleanupDays] = useState(30);

  // Modal AI
  const [showAiModal, setShowAiModal] = useState(false);

  const loadUnread = async () => {
    try {
      const r = await authApis().get(endpoints.unreadCount);
      setUnread(Number(r?.data?.count ?? 0));
    } catch {
      // ignore lỗi nhỏ
    }
  };

  const load = async (goPage = page) => {
    setLoading(true);
    try {
      const params = {
        page: goPage,
        pageSize,
        kw: kw || undefined,
        type: type || undefined,
        isRead,
      };
      const r = await authApis().get(endpoints.notifications, { params });
      const { arr, meta } = normalizeItems(r.data);
      setItems(arr);
      setPage(meta.page);
      setTotalPages(meta.totalPages);
      setTotalElements(meta.totalElements);
      // cập nhật badge chưa đọc
      loadUnread();
    } catch (err) {
      console.error(err);
      alert("Không tải được danh sách thông báo!");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load(1);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Prepend WebSocket notifications in real-time
  useEffect(() => {
    const handleNewNotif = (e) => {
      const newNotif = e.detail;
      if (page === 1) {
        setItems((prev) => {
          const id = newNotif.notificationId ?? newNotif.id;
          if (prev.some((item) => (item.notificationId ?? item.id) === id)) {
            return prev;
          }
          return [newNotif, ...prev.slice(0, pageSize - 1)];
        });
      }
      setTotalElements((prev) => prev + 1);
      setUnread((prev) => prev + 1);
    };

    window.addEventListener("trackfit-notification", handleNewNotif);
    return () => {
      window.removeEventListener("trackfit-notification", handleNewNotif);
    };
  }, [page, pageSize]);

  const mark = async (id, value) => {
    try {
      await authApis().put(endpoints.notificationMarkRead(id, value));
      load();
      window.dispatchEvent(new CustomEvent("trackfit-notif-update"));
    } catch (err) {
      console.error(err);
      alert("Không cập nhật trạng thái đọc!");
    }
  };

  const del = async (id) => {
    if (!window.confirm("Xóa thông báo này?")) return;
    try {
      await authApis().delete(endpoints.notificationDetail(id));
      // nếu xóa hết trang hiện tại thì lùi 1 trang
      const nextPage = items.length === 1 && page > 1 ? page - 1 : page;
      load(nextPage);
      window.dispatchEvent(new CustomEvent("trackfit-notif-update"));
    } catch (err) {
      console.error(err);
      alert("Không xóa được thông báo!");
    }
  };

  const readAll = async () => {
    if (!window.confirm("Đánh dấu ĐÃ ĐỌC tất cả thông báo?")) return;
    try {
      await authApis().put(endpoints.readAll);
      load();
      window.dispatchEvent(new CustomEvent("trackfit-notif-update"));
    } catch (err) {
      console.error(err);
      alert("Không thể đánh dấu đã đọc tất cả!");
    }
  };

  const doCleanup = async () => {
    const days = Number(cleanupDays);
    if (!days || days < 1) {
      alert("Số ngày phải >= 1");
      return;
    }
    if (!window.confirm(`Xóa thông báo ĐÃ ĐỌC trước ${days} ngày?`)) return;
    try {
      await authApis().delete(endpoints.cleanup(days));
      load(1);
      window.dispatchEvent(new CustomEvent("trackfit-notif-update"));
    } catch (err) {
      console.error(err);
      alert("Không thể dọn dẹp thông báo cũ!");
    }
  };

  // Hàm chuyển trang cho phân trang
  const go = (newPage) => {
    if (newPage < 1 || newPage > totalPages || newPage === page) return;
    load(newPage);
  };

  return (
    <>
      <div className="d-flex align-items-center justify-content-between mb-3">
        <div className="d-flex align-items-center gap-2">
          <h3 className="m-0">Thông báo</h3>
          {!!totalElements && <Badge bg="secondary">{totalElements}</Badge>}
          <Badge bg={unread > 0 ? "danger" : "success"} title="Chưa đọc">
            {unread} chưa đọc
          </Badge>
        </div>

        <div className="d-flex align-items-center gap-2">
          {isAdmin && (
            <Button
              variant="warning"
              size="sm"
              title="Gọi AI gửi lời khuyên"
              onClick={() => setShowAiModal(true)}
            >
              AI tạo lời khuyên
            </Button>
          )}
          <Button variant="outline-secondary" size="sm" onClick={() => load()}>
            Làm mới
          </Button>
        </div>
      </div>

      <Card className="shadow-sm border-0 mb-3">
        <Card.Body>
          <Form
            onSubmit={(e) => {
              e.preventDefault();
              load(1);
            }}
          >
            <Row className="g-2">
              <Col md={6}>
                <Form.Control
                  placeholder="Tìm nội dung/người gửi…"
                  value={kw}
                  onChange={(e) => setKw(e.target.value)}
                />
              </Col>
              <Col md={3}>
                <Form.Select value={type} onChange={(e) => setType(e.target.value)}>
                  {TYPE_OPTIONS.map((o) => (
                    <option key={o.value} value={o.value}>
                      {o.label}
                    </option>
                  ))}
                </Form.Select>
              </Col>
              <Col md={2}>
                <Form.Select
                  value={isRead === undefined ? "" : String(isRead)}
                  onChange={(e) => {
                    const v = e.target.value;
                    setIsRead(v === "" ? undefined : v === "true");
                  }}
                >
                  <option value="">Tất cả trạng thái</option>
                  <option value="false">Chưa đọc</option>
                  <option value="true">Đã đọc</option>
                </Form.Select>
              </Col>
              <Col md={1}>
                <div className="d-grid">
                  <Button type="submit" variant="outline-primary">
                    Lọc
                  </Button>
                </div>
              </Col>
            </Row>

            <Row className="g-2 mt-2">
              <Col md={6} className="d-flex gap-2">
                <Button variant="success" size="sm" onClick={readAll}>
                  Đọc hết
                </Button>
                <InputGroup style={{ width: 240 }}>
                  <Form.Control
                    type="number"
                    min={1}
                    value={cleanupDays}
                    onChange={(e) => setCleanupDays(e.target.value)}
                  />
                  <InputGroup.Text>ngày</InputGroup.Text>
                  <Button variant="outline-danger" onClick={doCleanup}>
                    Dọn cũ
                  </Button>
                </InputGroup>
              </Col>
            </Row>
          </Form>
        </Card.Body>
      </Card>

      {loading ? (
        <MySpinner />
      ) : (
        <Card className="shadow-sm border-0">
          <Table hover responsive className="m-0 align-middle">
            <thead className="table-light">
              <tr>
                <th style={{ width: 80 }}>ID</th>
                <th style={{ width: 120 }}>Loại</th>
                <th style={{ width: 120 }}>Nguồn</th>
                <th style={{ width: 160 }}>Người gửi</th>
                <th>Nội dung</th>
                <th style={{ width: 180 }}>Thời gian</th>
                <th style={{ width: 90 }}>Đọc</th>
                <th style={{ width: 230 }}></th>
              </tr>
            </thead>
            <tbody>
              {items?.length ? (
                items.map((n) => {
                  const id = n.notificationId ?? n.id;
                  const read = n.read ?? n.isRead;
                  const title = n.title ?? n.type ?? "";
                  const content = n.content ?? n.message ?? "";
                  return (
                    <tr key={id}>
                      <td>{id}</td>
                      <td>
                        <Badge bg="info" text="dark">
                          {title}
                        </Badge>
                      </td>
                      <td>{n.source || "-"}</td>
                      <td>{n.sender || "-"}</td>
                      <td className="text-break">{content}</td>
                      <td className="text-nowrap">{fmtTime(n.createdAt)}</td>
                      <td className="text-center">{read ? "✔" : "✖"}</td>
                      <td className="text-end">
                        <Button
                          size="sm"
                          variant={read ? "outline-secondary" : "success"}
                          onClick={() => mark(id, !read)}
                        >
                          {read ? "Đánh dấu chưa đọc" : "Đánh dấu đã đọc"}
                        </Button>{" "}
                        <Button size="sm" variant="outline-danger" onClick={() => del(id)}>
                          Xóa
                        </Button>
                      </td>
                    </tr>
                  );
                })
              ) : (
                <tr>
                  <td colSpan={8} className="text-center text-muted py-4">
                    Không có thông báo
                  </td>
                </tr>
              )}
            </tbody>
          </Table>

          {totalPages > 1 && (
            <Card.Body className="d-flex justify-content-center">
              <Pagination className="m-0">
                <Pagination.First onClick={() => go(1)} disabled={page === 1} />
                <Pagination.Prev onClick={() => go(page - 1)} disabled={page === 1} />
                <Pagination.Item active>{page}</Pagination.Item>
                <Pagination.Next onClick={() => go(page + 1)} disabled={page === totalPages} />
                <Pagination.Last onClick={() => go(totalPages)} disabled={page === totalPages} />
              </Pagination>
            </Card.Body>
          )}
        </Card>
      )}

      {/* Modal AI */}
      {isAdmin && (
        <AiAdviceModal
          show={showAiModal}
          onHide={() => setShowAiModal(false)}
          onCreated={() => load(1)}
        />
      )}
    </>
  );
};

export default NotificationsList;
