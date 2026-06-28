import { Spinner } from "react-bootstrap";

const MySpinner = () => {
  return (
    <div className="d-flex justify-content-center align-items-center" style={{ minHeight: "30vh" }}>
      <Spinner animation="border" role="status" style={{ width: "3rem", height: "3rem" }}>
        <span className="visually-hidden">Loading...</span>
      </Spinner>
    </div>
  );
};

export default MySpinner;
