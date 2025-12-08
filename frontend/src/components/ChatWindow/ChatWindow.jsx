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
  const { contacts, selectedContact, setSelectedContact } = useContext(ChatContext);
  const [messages, setMessages] = useState([]);
  const { user } = useContext(AuthContext);
  const currentUserId = user?.id;
  const [newMessage, setNewMessage] = useState("");
  const [emojiPickerVisible, setEmojiPickerVisible] = useState(false);
  const chatEndRef = useRef(null);
  const emojiPickerRef = useRef(null);
  const { client, connected } = useContext(SocketContext);

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

    const updated = contacts.find(c => c.id === selectedContact.id);
    if (updated) {
      setSelectedContact(prev => ({
        ...prev,
        status: updated.status,
        profileImageUrl: safeImage(updated.profileImageUrl ?? prev.profileImageUrl)
      }));
    }
  }, [contacts]);

  // Subscribe to room messages
  useEffect(() => {
    if (!selectedContact || !connected || !client) return;

    const sub = client.subscribe(
      `/topic/room/${selectedContact.roomId}`,
      (msg) => {
        setMessages(prev => [...prev, JSON.parse(msg.body)]);
      }
    );

    return () => sub.unsubscribe();
  }, [selectedContact, connected, client]);

  // Load history
  useEffect(() => {
    if (!selectedContact) return;

    const fetchMessages = async () => {
      try {
        const response = await axios.get(
          `http://localhost:8080/messages/${selectedContact.roomId}`,
          { withCredentials: true }
        );
        setMessages(response.data);
      } catch (err) {
        toast.error("Failed to load messages");
      }
    };

    fetchMessages();
  }, [selectedContact]);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const handleKeyPress = (e) => {
    if (e.key === "Enter") sendMessage();
  };

  const sendMessage = async () => {
    if (!newMessage.trim()) return;

    try {
      const payload = {
        roomId: selectedContact.roomId,
        content: newMessage
      };

      const response = await axios.post(
        "http://localhost:8080/messages/send",
        payload,
        { withCredentials: true }
      );

      setMessages(prev => [...prev, response.data]);
      setNewMessage("");

    } catch (err) {
      toast.error("Failed to send message");
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

      <div className="chat-messages">

        <div className="new-chat">
          <span><img src={selectedContact.profileImageUrl || "/assets/default-logo.png"} alt="" /></span>
          <h2>{selectedContact.username}</h2>
          <p>Start Chatting with {selectedContact.username} by sending Hi!</p>
          <button>Send Hello</button>
        </div>

        {messages.map((msg, idx) => {
          const isSentByMe = msg.senderId === currentUserId;

          return (
            <div key={idx} className={isSentByMe ? "sent-wrapper" : "received-wrapper"}>

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
              </div>

            </div>
          );
        })}

        <div ref={chatEndRef} />
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
            onKeyPress={handleKeyPress}
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
