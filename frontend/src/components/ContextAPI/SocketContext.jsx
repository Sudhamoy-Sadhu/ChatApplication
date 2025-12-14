import { createContext, useEffect, useState, useContext } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import { AuthContext } from "./AuthContext";

export const SocketContext = createContext(null);

export function SocketProvider({ children }) {
  const { isAuthenticated, loading } = useContext(AuthContext);
  const [client, setClient] = useState(null);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    if (loading) return;

    if (!isAuthenticated) {
      if (client) {
        client.deactivate();
        setClient(null);
        setConnected(false);
      }
      return;
    }

    if (client) return;

    console.log("🔐 Authenticated → Connecting WebSocket");

    const sock = new SockJS("http://localhost:8080/ws", null, {
      withCredentials: true,
    });

    const stompClient = new Client({
      webSocketFactory: () => sock,
      reconnectDelay: 3000,
      debug: (msg) => console.log("[STOMP]", msg),
    });

    stompClient.onConnect = () => {
      console.log("✅ WebSocket connected");
      setConnected(true);
    };

    stompClient.onDisconnect = () => {
      console.log("🔌 WebSocket disconnected");
      setConnected(false);
    };

    stompClient.onStompError = (frame) => {
      console.error("❌ Broker error:", frame.headers["message"]);
    };

    stompClient.activate();
    setClient(stompClient);

    return () => {
      stompClient.deactivate();
      setClient(null);
      setConnected(false);
    };
  }, [isAuthenticated, loading]);

  return (
    <SocketContext.Provider value={{ client, connected }}>
      {children}
    </SocketContext.Provider>
  );
}
