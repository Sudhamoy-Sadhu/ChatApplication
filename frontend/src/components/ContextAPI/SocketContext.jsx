import { createContext, useEffect, useState, useContext, useRef } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import { AuthContext } from "./AuthContext";

export const SocketContext = createContext();

export function SocketProvider({ children }) {
  const { isAuthenticated, loading } = useContext(AuthContext);
  const clientRef = useRef(null);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    // ⛔ Do nothing while auth is loading
    if (loading) return;

    // ⛔ Do nothing if not authenticated
    if (!isAuthenticated) {
      if (clientRef.current) {
        clientRef.current.deactivate();
        clientRef.current = null;
        setConnected(false);
      }
      return;
    }

    // ✅ Prevent double connection
    if (clientRef.current) return;

    console.log("🔐 Authenticated → Connecting WebSocket");

    const sock = new SockJS("http://localhost:8080/ws", null, {
      withCredentials: true,
    });

    const client = new Client({
      webSocketFactory: () => sock,
      reconnectDelay: 3000,
      debug: (msg) => console.log("[STOMP]", msg),
    });

    client.onConnect = () => {
      console.log("✅ WebSocket connected");
      setConnected(true);
    };

    client.onDisconnect = () => {
      console.log("🔌 WebSocket disconnected");
      setConnected(false);
    };

    client.onStompError = (frame) => {
      console.error("❌ Broker error:", frame.headers["message"]);
    };

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
      setConnected(false);
    };
  }, [isAuthenticated, loading]);

  return (
    <SocketContext.Provider value={{ client: clientRef.current, connected }}>
      {children}
    </SocketContext.Provider>
  );
}
