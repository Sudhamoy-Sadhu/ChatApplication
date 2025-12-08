// SocketContext.jsx
import { createContext, useEffect, useState, useContext } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import { AuthContext } from "./AuthContext";

export const SocketContext = createContext();

export function SocketProvider({ children }) {
  const { user } = useContext(AuthContext);   // <-- important
  const [client, setClient] = useState(null);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    if (!user) return;  // <-- wait for auth to finish loading

    console.log("Initializing WebSocket AFTER user is ready...");

    const sock = new SockJS("http://localhost:8080/ws", null, {
      withCredentials: true,
    });

    const c = new Client({
      webSocketFactory: () => sock,
      reconnectDelay: 3000,  // retry 3 seconds
      debug: (msg) => console.log("[STOMP] " + msg),
    });

    c.onConnect = () => {
      console.log("WS CONNECTED ✔");
      setConnected(true);
    };

    c.onStompError = (frame) => {
      console.error("Broker error:", frame.headers["message"]);
      console.error("Details:", frame.body);
    };

    c.activate();
    setClient(c);

    return () => c.deactivate();
  }, [user]);  // <-- run again ONLY when user loads

  return (
    <SocketContext.Provider value={{ client, connected }}>
      {children}
    </SocketContext.Provider>
  );
}
