import React from "react";
import ReactDOM from "react-dom/client";
import "./index.css";
import App from "./App.jsx";
import { Buffer } from "buffer";
import process from "process";

window.Buffer = Buffer;
window.process = process;


import { ChatProvider } from "./components/ContextAPI/ChatContext";
import { AuthProvider } from "./components/ContextAPI/AuthContext";
import { SocketProvider } from "./components/ContextAPI/SocketContext";

import axios from "axios";
import { ToastProvider } from "./components/ContextAPI/ToastContext.jsx";

axios.defaults.withCredentials = true;

const root = ReactDOM.createRoot(document.getElementById("root"));

root.render(
  <React.StrictMode>
    <AuthProvider>
      <SocketProvider>
        <ChatProvider>
          <ToastProvider>
            <App />
          </ToastProvider>
        </ChatProvider>
      </SocketProvider>
    </AuthProvider>
  </React.StrictMode>
);
