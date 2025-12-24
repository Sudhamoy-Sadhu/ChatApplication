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

    const stompClient = new Client({
      webSocketFactory: () =>
        new SockJS("http://localhost:8080/ws", null, { withCredentials: true }),
      reconnectDelay: 5000,
      debug: (msg) => {
        if (import.meta.env.DEV) console.log("[STOMP]", msg);
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

  const subscribe = (destination, callback) => {
    if (!clientRef.current || !connected) return null;

    if (subscriptionsRef.current.has(destination)) {
      console.warn(`Already subscribed to ${destination}`);
      return null;
    }

    const sub = clientRef.current.subscribe(destination, callback);

    // ✅ store the SUBSCRIPTION
    subscriptionsRef.current.set(destination, sub);

    return () => {
      sub.unsubscribe();
      subscriptionsRef.current.delete(destination);
    };
  };


  const unsubscribeAll = () => {
    subscriptionsRef.current.forEach((sub) => {
      try {
        sub.unsubscribe();
      } catch { }
    });
    subscriptionsRef.current.clear();
  };


  useEffect(() => {
    if (!connected || !user?.id || !clientRef.current) return;

    console.log("🛠️ Initializing Global Subscriptions");

    // Receipt listener stays the same
    const receiptSub = clientRef.current.subscribe(
      `/topic/receipt/${user.id}`,
      (msg) => {
        const data = JSON.parse(msg.body);
        setReceiptUpdate({
          messageId: data.messageId,
          roomId: data.roomId,
          status: data.status,
          allMessagesInRoom: data.allMessagesInRoom,
          ts: Date.now(),
        });
      }
    );

    // CHANGED: Global message listener now ONLY sends DELIVERED
    const msgSub = clientRef.current.subscribe(
      `/user/queue/messages`,
      (msg) => {
        const message = JSON.parse(msg.body);

        if (Number(message.senderId) !== Number(user.id)) {
          clientRef.current.publish({
            destination: "/app/chat.ack",
            body: JSON.stringify({
              messageId: message.id,
              status: "DELIVERED", // Only mark as delivered here
            }),
          });
        }
      }
    );

    subscriptionsRef.current.set("global-receipt", receiptSub);
    subscriptionsRef.current.set("global-messages", msgSub);

    return () => {
      receiptSub.unsubscribe();
      msgSub.unsubscribe();
      subscriptionsRef.current.delete("global-receipt");
      subscriptionsRef.current.delete("global-messages");
    };
  }, [connected, user?.id]);


  return (
    <SocketContext.Provider value={{ client: clientRef.current, connected, subscribe, unsubscribeAll, receiptUpdate }}>
      {children}
    </SocketContext.Provider>
  );
}
