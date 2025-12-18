import { createContext, useEffect, useState, useRef, useContext } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import { AuthContext } from "./AuthContext";

export const SocketContext = createContext(null);

export function SocketProvider({ children }) {
  const { isAuthenticated, loading, user } = useContext(AuthContext);
  const [connected, setConnected] = useState(false);
  const clientRef = useRef(null);
  const subscriptionsRef = useRef(new Map());
  const [receiptUpdate, setReceiptUpdate] = useState(null);

  useEffect(() => {
    if (loading) return;

    if (!isAuthenticated) {
      if (clientRef.current) {
        clientRef.current.deactivate();
        clientRef.current = null;
        setConnected(false);
      }
      return;
    }

    if (clientRef.current) return; // already connected

    console.log("🔐 Connecting WebSocket...");

    const sock = new SockJS("http://localhost:8080/ws", null, { withCredentials: true });

    const stompClient = new Client({
      webSocketFactory: () => sock,
      reconnectDelay: 5000, // auto reconnect
      debug: (msg) => {
        if (process.env.NODE_ENV === "development") console.log("[STOMP]", msg);
      },
    });

    // Connection handlers
    stompClient.onConnect = () => {
      console.log("✅ WebSocket connected");
      setConnected(true);
    };

    stompClient.onDisconnect = () => {
      console.log("🔌 WebSocket disconnected");
      setConnected(false);
    };

    stompClient.onStompError = (frame) => {
      console.error("❌ STOMP broker error:", frame.headers["message"]);
    };

    stompClient.activate();
    clientRef.current = stompClient;

    return () => {
      stompClient.deactivate();
      clientRef.current = null;
      setConnected(false);
    };
  }, [isAuthenticated, loading]);

  // Production-grade subscription function
  const subscribe = (destination, callback) => {
    if (!clientRef.current || !connected) return null;

    // Avoid duplicate subscription
    if (subscriptionsRef.current.has(destination)) {
      console.warn(`Already subscribed to ${destination}`);
      return null;
    }

    const sub = clientRef.current.subscribe(destination, callback);
    subscriptionsRef.current.set(destination, callback);

    return () => {
      sub.unsubscribe();
      subscriptionsRef.current.delete(destination);
    };
  };

  const unsubscribeAll = () => {
    if (!clientRef.current) return;
    subscriptionsRef.current.clear();
  };

 useEffect(() => {
    // Only proceed if connected and we have a user
    if (!connected || !user?.id || !clientRef.current) return;

    console.log("🛠️ Initializing Global Subscriptions");

    // 1. GLOBAL RECEIPT LISTENER
    // This updates the ticks (Sent -> Delivered -> Read) for messages YOU sent
    const receiptSub = clientRef.current.subscribe(`/topic/receipt/${user.id}`, (msg) => {
      const data = JSON.parse(msg.body);
      setReceiptUpdate({
        messageId: data.messageId,
        status: data.status,
        ts: Date.now() 
      });
    });

    // 2. GLOBAL DELIVERED ACK GENERATOR
    // This sends "DELIVERED" back to anyone who sends YOU a message, even if ChatWindow is closed.
    const msgSub = clientRef.current.subscribe(`/user/queue/messages`, (msg) => {
      const message = JSON.parse(msg.body);

      // If I am the receiver, tell the server I received it (Double Gray Tick for sender)
      if (Number(message.senderId) !== Number(user.id)) {
        clientRef.current.publish({
          destination: "/app/chat.ack",
          body: JSON.stringify({
            messageId: message.id,
            status: "DELIVERED",
          }),
        });
      }
    });

    // CLEANUP: Unsubscribe when component unmounts or user changes
    return () => {
      console.log("🧹 Cleaning up Global Subscriptions");
      receiptSub.unsubscribe();
      msgSub.unsubscribe();
    };
  }, [connected, user?.id]);

return (
  <SocketContext.Provider value={{ client: clientRef.current, connected, subscribe, unsubscribeAll, receiptUpdate }}>
    {children}
  </SocketContext.Provider>
);
}
