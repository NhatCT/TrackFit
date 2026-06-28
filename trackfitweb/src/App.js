import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Header from "./components/layout/Header";
import Footer from "./components/layout/Footer";
import Home from "./components/Home";
import Register from "./components/Register";
import Login from "./components/Login";
import { Container, Toast, ToastContainer } from "react-bootstrap";
import "bootstrap/dist/css/bootstrap.min.css";
import { MyUserContext } from "./configs/Context";
import MyUserReducer from "./components/reducers/MyUserReducer";
import { useReducer, useEffect, useState } from "react";
import AOS from "aos";
import "aos/dist/aos.css";
import cookie from "react-cookies";
import "./components/styles/theme.css";

import Profile from "./components/Profile";
import ChangePassword from "./components/ChangePassword";
import ExercisesBrowse from "./components/ExercisesBrowse"; 
import ExerciseForm from "./components/ExerciseForm";   
import GoalsList from "./components/GoalsList";
import HealthList from "./components/HealthList";
import NotificationsList from "./components/NotificationsList";
import Recommendations from "./components/Recommendations";
import StatsSummary from "./components/StatsSummary";
import PlansList from "./components/PlansList";
import PlanForm from "./components/PlanForm";
import HistoriesList from "./components/HistoriesList";
import Chatbot from "./components/ChatBot";
import ChatWidget from "./components/ChatWidget";
import WebSocketListener from "./components/layout/WebSocketListener";

const App = () => {
  const [user, dispatch] = useReducer(MyUserReducer, cookie.load("user") || null);

  const [toasts, setToasts] = useState([]);

  const logout = () => {
    dispatch({ type: "logout" });
    cookie.remove("user");
    cookie.remove("token");
    window.location.href = "/login";
  };

  useEffect(() => {
    AOS.init({ duration: 1000, once: true });
  }, []);

  useEffect(() => {
    const handleNotification = (e) => {
      const notif = e.detail;
      setToasts((prev) => [...prev, { ...notif, id: Date.now() }]);
    };

    window.addEventListener("trackfit-notification", handleNotification);
    return () => {
      window.removeEventListener("trackfit-notification", handleNotification);
    };
  }, []);

  return (
    <MyUserContext.Provider value={[user, dispatch]}>
      <BrowserRouter>
        <WebSocketListener user={user} />
        <Header user={user} onLogout={logout} />

        {/* Real-time Toast Notifications */}
        <ToastContainer position="top-end" className="p-3" style={{ position: "fixed", zIndex: 9999 }}>
          {toasts.map((t) => (
            <Toast
              key={t.id}
              onClose={() => setToasts((prev) => prev.filter((x) => x.id !== t.id))}
              delay={6000}
              autohide
              className="shadow-lg border-0 mb-2"
              style={{
                background: "rgba(33, 37, 41, 0.95)",
                backdropFilter: "blur(10px)",
                color: "#fff",
                borderRadius: "12px",
                borderLeft: t.type === "ADVICE" ? "4px solid #198754" 
                           : t.type === "REMINDER" ? "4px solid #ffc107" 
                           : "4px solid #0d6efd",
              }}
            >
              <Toast.Header closeButton={true} className="border-0 text-white bg-transparent">
                <strong className="me-auto d-flex align-items-center gap-2">
                  {t.type === "ADVICE" ? "💡" : t.type === "REMINDER" ? "⏰" : "⚙️"} {t.title || "Thông báo"}
                </strong>
                <small className="text-light-50">vừa xong</small>
              </Toast.Header>
              <Toast.Body className="pt-0 text-light">{t.message || t.content}</Toast.Body>
            </Toast>
          ))}
        </ToastContainer>
        <main>
          <Container className="py-4" style={{ minHeight: "80vh" }}>
            <Routes>
              <Route path="/" element={<Home />} />
              <Route path="/register" element={<Register />} />
              <Route path="/login" element={user ? <Navigate to="/" /> : <Login />} />
              <Route path="/profile" element={user ? <Profile /> : <Navigate to="/login" />} />
              <Route path="/profile/password" element={user ? <ChangePassword /> : <Navigate to="/login" />} />
              <Route path="/exercises" element={user ? <ExercisesBrowse /> : <Navigate to="/login" />} />
              <Route path="/exercises/new" element={user ? <ExerciseForm /> : <Navigate to="/login" />} />
              <Route path="/exercises/:id" element={user ? <ExerciseForm /> : <Navigate to="/login" />} />
              <Route path="/goals" element={user ? <GoalsList /> : <Navigate to="/login" />} />
              <Route path="/health" element={user ? <HealthList /> : <Navigate to="/login" />} />
              <Route path="/notifications" element={user ? <NotificationsList /> : <Navigate to="/login" />} />
              <Route path="/recommendations" element={user ? <Recommendations /> : <Navigate to="/login" />} />
              <Route path="/chatbot" element={user ? <Chatbot /> : <Navigate to="/login" />} />
              <Route path="/stats/summary" element={user ? <StatsSummary /> : <Navigate to="/login" />} />
              <Route path="/plans" element={user ? <PlansList /> : <Navigate to="/login" />} />
              <Route path="/plans/new" element={user ? <PlanForm /> : <Navigate to="/login" />} />
              <Route path="/plans/:id" element={user ? <PlanForm /> : <Navigate to="/login" />} />
              <Route path="/histories" element={user ? <HistoriesList /> : <Navigate to="/login" />} />
              <Route path="*" element={<Navigate to="/" />} />
            </Routes>
          </Container>
        </main>
        <Footer />
         <ChatWidget requireAuth />
      </BrowserRouter>
    </MyUserContext.Provider>
  );
};

export default App;
