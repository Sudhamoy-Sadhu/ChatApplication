import React, { useEffect } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom"
import "./App.css";
import ChatHome from "./components/ChatHome/ChatHome";
import LoginandSignUp from "./components/Login&SignUp/Login&SignUp";
import ProtectedRoute from "./components/ProtectedRoutes/ProtectedRoutes";
import ForgotPassword from "./components/ForgotPassword/ForgotPassword";
import ModalProvider from "./components/ContextAPI/ModalContext";
import PageManagerProvider from "./components/ContextAPI/PageManagerContext";
import stompClient from "./components/WebSocket/Socket";
import { SocketProvider } from "./components/ContextAPI/SocketContext";


export default function App() {
  useEffect(() => {
    stompClient.onConnect = () => {
      console.log("🔥 STOMP CONNECTED");
    };
  }, []);

  return (
    <SocketProvider>
      <ModalProvider>
        <PageManagerProvider>
          <Router>
            <Routes>
              <Route path="/login" element={<LoginandSignUp />} />
              <Route path="/forgot-password" element={<ForgotPassword />} />
              <Route path="/" element={<ProtectedRoute><ChatHome /></ProtectedRoute>} />
            </Routes>
          </Router>
        </PageManagerProvider>
      </ModalProvider>
    </SocketProvider>
  );
}
