import { Card, Badge, Button } from "react-bootstrap";
import { getYoutubeThumbnail } from "../utils/youtubeUtils";

const ExerciseCard = ({
  item,
  onPreview,
  onEdit,
  onDelete,
  allowManage = false
}) => {
  const thumb = getYoutubeThumbnail(item.videoUrl);

  return (
    <Card className="exercise-card hoverable border-0 h-100">
      <div
        className="exercise-thumb"
        role="button"
        onClick={() => onPreview(item.exercisesId)}
        title="Xem nhanh"
      >
        {thumb ? (
          <img src={thumb} alt={item.name} className="w-100 h-100 object-cover" />
        ) : (
          <div className="thumb-fallback d-flex align-items-center justify-content-center">
            Chưa có video
          </div>
        )}
      </div>

      <Card.Body className="d-flex flex-column">
        <div className="d-flex flex-wrap gap-2 mb-2">
          {item.muscleGroup && <Badge bg="dark" className="text-uppercase">{item.muscleGroup}</Badge>}
          {item.targetGoal && <Badge bg="secondary">{item.targetGoal}</Badge>}
        </div>

        <Card.Title className="fs-6 text-truncate" title={item.name}>
          {item.name}
        </Card.Title>

        <Card.Text className="text-muted small flex-grow-1 line-clamp-2" title={item.description}>
          {item.description || "—"}
        </Card.Text>

        <div className="d-flex justify-content-between align-items-center">
          <span className="text-muted small">
            {item.createdAt ? new Date(item.createdAt).toLocaleDateString() : "—"}
          </span>
          <div className="d-flex gap-1">
            <Button size="sm" variant="outline-primary" onClick={() => onPreview(item.exercisesId)}>
              Xem nhanh
            </Button>
            {allowManage && (
              <>
                <Button
                  size="sm"
                  variant="outline-secondary"
                  onClick={() => onEdit?.(item.exercisesId)}
                >
                  Sửa
                </Button>
                <Button
                  size="sm"
                  variant="outline-danger"
                  onClick={() => onDelete?.(item.exercisesId)}
                >
                  Xóa
                </Button>
              </>
            )}
          </div>
        </div>
      </Card.Body>
    </Card>
  );
};

export default ExerciseCard;
