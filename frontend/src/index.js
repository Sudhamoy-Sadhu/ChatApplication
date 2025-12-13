import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import { ChatProvider } from "./components/ContextAPI/ChatContext";
import { AuthProvider } from './components/ContextAPI/AuthContext';
import axios from 'axios';
import { SocketProvider } from './components/ContextAPI/SocketContext';

const root = ReactDOM.createRoot(document.getElementById("root"));
axios.defaults.withCredentials = true;
root.render(
  <React.StrictMode>
    <AuthProvider>
      <SocketProvider>
        <ChatProvider>
          <App />
        </ChatProvider>
      </SocketProvider>
    </AuthProvider>
  </React.StrictMode>
);
