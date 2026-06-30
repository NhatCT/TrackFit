import { Button } from "react-bootstrap";

const SimplePagination = ({ page, totalPages, onPageChange, className = "" }) => {
  if (!totalPages || totalPages <= 1) return null;

  return (
    <div className={`d-flex gap-2 justify-content-center mt-3 ${className}`}>
      <Button
        size="sm"
        disabled={page <= 1}
        onClick={() => onPageChange(page - 1)}
      >
        Truoc
      </Button>
      <div className="align-self-center small">
        Trang {page}/{totalPages}
      </div>
      <Button
        size="sm"
        disabled={page >= totalPages}
        onClick={() => onPageChange(page + 1)}
      >
        Sau
      </Button>
    </div>
  );
};

export default SimplePagination;
