import { useState, useRef, useEffect, useContext } from "react";
import "./ChatWindow.css";
import axios from "axios";
import { IoCall } from "react-icons/io5";
import { FaVideo } from "react-icons/fa";
import { BsThreeDotsVertical } from "react-icons/bs";
import { IoMdAdd } from "react-icons/io";
import { MdKeyboardVoice } from "react-icons/md";
import { GoDotFill } from "react-icons/go";
import { ChatContext } from "../ContextAPI/ChatContext";
import { MdEmojiEmotions } from "react-icons/md";
import 'emoji-picker-element';
import { toast, ToastContainer } from "react-toastify";
import { AuthContext } from "../ContextAPI/AuthContext";
import { SocketContext } from "../ContextAPI/SocketContext";

export default function ChatWindow() {
  const { contacts, setContacts, selectedContact, setSelectedContact } = useContext(ChatContext);
  const [messages, setMessages] = useState([]);
  const { user } = useContext(AuthContext);
  const currentUserId = user?.id;
  const [newMessage, setNewMessage] = useState("");
  const [emojiPickerVisible, setEmojiPickerVisible] = useState(false);
  const emojiPickerRef = useRef(null);
  const { client, connected } = useContext(SocketContext);
  const messagesContainerRef = useRef(null);
  const isAtBottomRef = useRef(true);
  const initialLoadRef = useRef(true);
  const pendingReceiptsRef = useRef(new Map());
  const { receiptUpdate } = useContext(SocketContext);



  const isOnline = selectedContact?.status === "ACTIVE";

  const safeImage = (url) => url || "/assets/default-logo.png";

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        emojiPickerVisible &&
        emojiPickerRef.current &&
        !emojiPickerRef.current.contains(event.target) &&
        !event.target.closest('.emoji-button')
      ) {
        setEmojiPickerVisible(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [emojiPickerVisible]);

  // Update header if status changes
  useEffect(() => {
    if (!selectedContact) return;

    const updated = contacts.find(c => c.userId === selectedContact.userId);
    if (!updated) return;

    if (
      updated.status !== selectedContact.status ||
      updated.profileImageUrl !== selectedContact.profileImageUrl
    ) {
      setSelectedContact(prev => ({
        ...prev,
        status: updated.status,
        profileImageUrl: safeImage(updated.profileImageUrl)
      }));
    }
  }, [contacts]);

  const scrollToBottom = () => {
    const el = messagesContainerRef.current;
    if (!el) return;

    el.scrollTop = el.scrollHeight;

    setTimeout(() => {
      el.scrollTop = el.scrollHeight;
    }, 50);
  };



  // useEffect(() => {
  //   if (!selectedContact) return;

  //   // Clear previous messages
  //   setMessages([]);
  // }, [selectedContact]);

  const lastReadMsgRef = useRef(null);

  useEffect(() => {
    if (!selectedContact || !client || !connected) return;

    let isMounted = true;
    initialLoadRef.current = true;
    lastReadMsgRef.current = null;

    const fetchMessages = async () => {
      try {
        const res = await axios.get(
          `http://localhost:8080/messages/${selectedContact.roomId}`,
          { withCredentials: true }
        );

        if (!isMounted) return;
        const processedMessages = res.data.map(m => {
          const pending = pendingReceiptsRef.current.get(m.id);
          if (pending) pendingReceiptsRef.current.delete(m.id);

          return {
            ...m,
            // Use the helper to determine if it's ✔, ✔✔, or blue ✔✔
            receiptStatus: pending ?? computeReceipt(m)
          };
        });

        setMessages(() => {
          return res.data.map(m => {
            const pending = pendingReceiptsRef.current.get(m.id);

            if (pending) {
              pendingReceiptsRef.current.delete(m.id);
            }

            return {
              ...m,
              receiptStatus: pending ?? computeReceipt(m)
            };
          });
        });


        requestAnimationFrame(() => {
          scrollToBottom();
          initialLoadRef.current = false;
        });

      } catch {
        toast.error("Failed to load messages");
      }
    };

    fetchMessages();

    const sub = client.subscribe(
      `/topic/room/${selectedContact.roomId}`,
      (msg) => {
        const message = JSON.parse(msg.body);
        const sentByMe = Number(message.senderId) === Number(currentUserId);

        if (!sentByMe && document.hasFocus()) {
          // ONLY send READ here. DELIVERED is now handled by SocketProvider globally.
          client.publish({
            destination: "/app/chat.ack",
            body: JSON.stringify({
              messageId: message.id,
              status: "READ",
            }),
          });
        }


        setMessages((prev) => {
          if (prev.some((m) => m.id === message.id)) return prev;
          return [...prev, { ...message, receiptStatus: sentByMe ? "SENT" : undefined }];
        });

        if (isAtBottomRef.current) {
          requestAnimationFrame(scrollToBottom);
        }
      }
    );

    return () => {
      isMounted = false;
      sub.unsubscribe();
    };
  }, [selectedContact?.roomId, client, connected, currentUserId]);



  useEffect(() => {
    if (!selectedContact) return;
    if (messages.length === 0) return;
    if (initialLoadRef.current) return;

    const lastMsg = messages[messages.length - 1];

    const isReceiver =
      Number(lastMsg.senderId) !== Number(currentUserId) &&
      Number(selectedContact.userId) === Number(lastMsg.senderId);

    if (!isReceiver) return;
    if (lastReadMsgRef.current === lastMsg.id) return;

    lastReadMsgRef.current = lastMsg.id;

    axios.post(
      `http://localhost:8080/messages/${selectedContact.roomId}/mark-read`,
      {},
      { withCredentials: true }
    ).catch(() => { });

    if (isReceiver) {
      setContacts(prev =>
        prev.map(c =>
          c.roomId === selectedContact.roomId
            ? { ...c, unreadCount: 0 }
            : c
        )
      );
    }

  }, [messages, selectedContact?.roomId]);




  const RECEIPT_PRIORITY = { SENT: 1, DELIVERED: 2, READ: 3 };

  // Inside component:
  useEffect(() => {
    if (!receiptUpdate) return;
    const { messageId, status } = receiptUpdate;

    setMessages(prev => {
      const exists = prev.some(m => m.id === messageId);
      if (!exists) {
        pendingReceiptsRef.current.set(messageId, status);
        return prev;
      }
      return prev.map(m => {
        if (m.id !== messageId) return m;
        if (RECEIPT_PRIORITY[status] <= RECEIPT_PRIORITY[m.receiptStatus]) return m;
        return { ...m, receiptStatus: status };
      });
    });
  }, [receiptUpdate]);


  const computeReceipt = (m) => {
    if (Number(m.senderId) !== Number(currentUserId)) return undefined;

    // Check if anyone else in the room (other than me) has read it
    const othersRead = m.readByUserIds?.some(id => Number(id) !== Number(currentUserId));
    if (othersRead) return "READ";

    // Check if anyone else has received it
    const othersDelivered = m.deliveredToUserIds?.some(id => Number(id) !== Number(currentUserId));
    if (othersDelivered) return "DELIVERED";

    return "SENT";
  };

  useEffect(() => {
    const handleFocus = () => {
      // When user returns to tab, find last message received from contact and mark read
      if (messages.length > 0) {
        const lastMsg = messages[messages.length - 1];
        const isFromContact = Number(lastMsg.senderId) === Number(selectedContact?.userId);

        if (isFromContact && client && connected) {
          client.publish({
            destination: "/app/chat.ack",
            body: JSON.stringify({
              messageId: lastMsg.id,
              status: "READ",
            }),
          });

          // Also call the REST endpoint to clear unread counts in DB
          axios.post(`http://localhost:8080/messages/${selectedContact.roomId}/mark-read`, {}, { withCredentials: true });
        }
      }
    };

    window.addEventListener('focus', handleFocus);
    return () => window.removeEventListener('focus', handleFocus);
  }, [messages, selectedContact, client, connected]);

  const handleKeyPress = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const sendingRef = useRef(false);
  const sendMessage = async () => {
    if (sendingRef.current) return;
    if (!newMessage.trim() || !selectedContact) return;

    sendingRef.current = true;

    try {
      const payload = {
        roomId: selectedContact.roomId,
        content: newMessage.trim(),
      };

      // Send to backend (DO NOT update messages here)
      await axios.post(
        "http://localhost:8080/messages/send",
        payload,
        { withCredentials: true }
      );

      // Clear input only
      setNewMessage("");

      // Message will arrive via WebSocket:
      // /topic/room/{roomId}
    } catch (err) {
      console.error("Send message failed:", err);
      toast.error("Failed to send message");
    } finally {
      sendingRef.current = false;
    }
  };


  if (!selectedContact) {
    return (
      <div className="chat-window-placeholder">
        <img src="/assets/77881.jpg" alt="" />
        <p>Select a contact to start chat</p>
      </div>
    );
  }

  const addEmoji = (emoji) => {
    setNewMessage((prev) => prev + emoji.unicode);
  };


  return (
    <div className="chat-container">

      <div className="chat-header">
        <div className="sender-info">
          <div className="avatar">
            <img src={selectedContact.profileImageUrl || "/assets/default-logo.png"} alt=""></img>
          </div>

          <h3>
            {selectedContact.username}
            <span>
              (<GoDotFill className={`online ${isOnline ? "" : "offline"}`} />
              {isOnline ? "Online" : "Offline"} )
            </span>
          </h3>
        </div>
        <div className="chat-actions">
          <span><FaVideo /></span>
          <span><IoCall /></span>
          <span><BsThreeDotsVertical /></span>
        </div>
      </div>

      <div className="chat-messages" ref={messagesContainerRef}
        onScroll={() => {
          const el = messagesContainerRef.current;
          if (!el) return;

          const threshold = 100;
          const atBottom =
            el.scrollHeight - el.scrollTop - el.clientHeight < threshold;

          isAtBottomRef.current = atBottom;
        }}>

        {messages.length === 0 && (
          <div className="new-chat">
            <span><img src={selectedContact.profileImageUrl || "/assets/default-logo.png"} alt="" /></span>
            <h2>{selectedContact.username}</h2>
            <p>Start Chatting with {selectedContact.username} by sending Hi!</p>
            <button>Send Hello</button>
          </div>
        )}

        {messages.map(msg => {
          const isSentByMe = Number(msg.senderId) === Number(currentUserId);
          const key = msg.id ?? `${msg.senderId} - ${msg.sentAt}`;
          return (
            <div key={key} className={isSentByMe ? "sent-wrapper" : "received-wrapper"}>

              <span className={isSentByMe ? "senttime" : "receivedtime"}>
                {new Date(msg.sentAt).toLocaleString("en-GB", {
                  day: "2-digit",
                  month: "short",
                  year: "numeric",
                  hour: "2-digit",
                  minute: "2-digit"
                })}
              </span>

              <div className={isSentByMe ? "sent" : "received"}>
                <p>{msg.content}</p>
                {isSentByMe && (
                  <span className={`receipt tick-${msg.receiptStatus?.toLowerCase()}`}>
                    {msg.receiptStatus === "READ"
                      ? "✔✔"
                      : msg.receiptStatus === "DELIVERED"
                        ? "✔✔"
                        : "✔"}
                  </span>
                )}
              </div>

            </div>
          );
        })}

      </div>

      <div className="chat-input">
        <span className="add-files"><IoMdAdd /></span>

        <div className="chat-input-emoji-input">
          <span className="emoji-button"
            onClick={(e) => { e.stopPropagation(); setEmojiPickerVisible((prev) => !prev) }}
          >
            <MdEmojiEmotions />
          </span>

          <input
            type="text"
            placeholder="Type your message..."
            value={newMessage}
            onChange={(e) => setNewMessage(e.target.value)}
            onKeyDown={handleKeyPress}
          />

          {emojiPickerVisible && (
            <div
              className="emoji-picker-wrapper"
              ref={emojiPickerRef}
              onClick={(e) => e.stopPropagation()}>
              <emoji-picker
                onemoji-click={(e) => addEmoji(e.detail)}
              ></emoji-picker>
            </div>
          )}

          <span className="mic"><MdKeyboardVoice /></span>
        </div>

        <button className="send" onClick={sendMessage}>➤</button>
      </div>

      <ToastContainer position="top-right" autoClose={2000} theme="light" />
    </div>
  );
}
