import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom"
import "./App.css";
import ChatHome from "./components/ChatHome/ChatHome";
import LoginandSignUp from "./components/Login&SignUp/Login&SignUp";


export default function App() {

  return (
      <Router>
        <Routes>
          <Route path="/" element={<ChatHome/>} />
          <Route path="/login" element={<LoginandSignUp/>} />
        </Routes>
      </Router>
  );
}
