import React from "react";

/**
 * Error Boundary — bắt lỗi JS trong component tree con,
 * hiển thị fallback UI thay vì white-screen-of-death.
 *
 * Yêu cầu class component vì React chưa hỗ trợ Error Boundary dạng hooks.
 */
class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null, errorInfo: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    this.setState({ errorInfo });
    console.error("[ErrorBoundary]", error, errorInfo);
  }

  handleReload = () => {
    window.location.reload();
  };

  handleGoHome = () => {
    window.location.href = "/";
  };

  render() {
    if (this.state.hasError) {
      return (
        <div
          style={{
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            justifyContent: "center",
            minHeight: "60vh",
            padding: "2rem",
            textAlign: "center",
          }}
        >
          <div
            style={{
              background: "var(--surface, #111a2b)",
              border: "1px solid var(--border, #203049)",
              borderRadius: "1rem",
              padding: "2.5rem",
              maxWidth: 520,
              width: "100%",
              boxShadow: "0 8px 32px rgba(0,0,0,.25)",
            }}
          >
            <div style={{ fontSize: "3rem", marginBottom: "1rem" }}>😵</div>
            <h4 style={{ color: "var(--text, #e6edf3)", marginBottom: "0.5rem" }}>
              Đã xảy ra lỗi
            </h4>
            <p style={{ color: "var(--muted, #9fb0c5)", marginBottom: "1.5rem", fontSize: "0.9rem" }}>
              Một lỗi không mong muốn đã xảy ra. Vui lòng tải lại trang hoặc quay về trang chủ.
            </p>

            {process.env.NODE_ENV === "development" && this.state.error && (
              <details
                style={{
                  textAlign: "left",
                  marginBottom: "1.5rem",
                  padding: "0.75rem",
                  background: "rgba(220,53,69,0.08)",
                  borderRadius: "0.5rem",
                  border: "1px solid rgba(220,53,69,0.2)",
                  fontSize: "0.78rem",
                  color: "#f8d7da",
                  maxHeight: 200,
                  overflow: "auto",
                }}
              >
                <summary style={{ cursor: "pointer", fontWeight: 600, marginBottom: "0.5rem" }}>
                  Chi tiết lỗi (Dev only)
                </summary>
                <pre style={{ whiteSpace: "pre-wrap", margin: 0 }}>
                  {this.state.error?.toString()}
                  {"\n"}
                  {this.state.errorInfo?.componentStack}
                </pre>
              </details>
            )}

            <div style={{ display: "flex", gap: "0.75rem", justifyContent: "center" }}>
              <button
                onClick={this.handleReload}
                style={{
                  padding: "0.6rem 1.5rem",
                  borderRadius: "0.5rem",
                  border: "none",
                  background: "var(--accent, #ff6b35)",
                  color: "#fff",
                  fontWeight: 600,
                  cursor: "pointer",
                  fontSize: "0.9rem",
                }}
              >
                Tải lại trang
              </button>
              <button
                onClick={this.handleGoHome}
                style={{
                  padding: "0.6rem 1.5rem",
                  borderRadius: "0.5rem",
                  border: "1px solid var(--border, #203049)",
                  background: "transparent",
                  color: "var(--text, #e6edf3)",
                  fontWeight: 600,
                  cursor: "pointer",
                  fontSize: "0.9rem",
                }}
              >
                Về trang chủ
              </button>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
