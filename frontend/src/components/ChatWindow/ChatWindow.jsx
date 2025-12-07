import { useState, useRef, useEffect, useContext } from "react";
import "./ChatWindow.css";
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

export default function ChatWindow() {
  const { selectedContact } = useContext(ChatContext);
  console.log(selectedContact);
  const [messages, setMessages] = useState([]);

  const [newMessage, setNewMessage] = useState("");
  const [emojiPickerVisible, setEmojiPickerVisible] = useState(false);

  // 👇 refs for scrolling
  const chatEndRef = useRef(null);
  const emojiPickerRef = useRef(null);

  // scroll to bottom on mount + new message
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, selectedContact]);

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

  const sendMessage = () => {
    if (newMessage.trim() && selectedContact) {
      const newMsg = {
        sender: "You",
        text: newMessage,
        timestamp: new Date().toLocaleString()
      };
      setMessages((prev) => [...prev, newMsg]);
      setNewMessage("");
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter") sendMessage();
  };

  if (!selectedContact)
    return <div className="chat-window-placeholder">
      {/* <img src="/assets/chatting.avif" alt=""/> */}
      {/* <img src="/assets/SecureChatting.png" alt=""/> */}
      <img src="/assets/77881.jpg" alt="" />
      <p>Select a contact to start chat</p>
    </div>;

  const addEmoji = (emoji) => {
    setNewMessage((prev) => prev + emoji.unicode);
  };

  return (
    <div className="chat-container">

      <div className="chat-header">
        <div key={selectedContact.id} className="sender-info">
          <div className="avatar"><img src={selectedContact.pic} alt=""></img></div>
          <h3>
            {selectedContact.name}
            <span>(<GoDotFill className={`online ${selectedContact.online === "ACTIVE" ? "" : "offline"}`} /> {`${selectedContact.online === "ACTIVE" ? "Online" : "Offline"}`} )</span>
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
          <span><img src={selectedContact.pic} alt=""></img></span>
          <h2>{selectedContact.name}</h2>
          <p>Start Chatting with {selectedContact.name} by Sending Hi!</p>
          <button>Send Hello</button>
        </div>

        {messages.map((msg, idx) => (
          <>
            <span className={`${msg.sender === "You" ? "senttime" : "receivedtime"}`}>
              {msg.sender === "You"
                ? `${new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })} ${new Date().toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}`
                : msg.timestamp || ""}
            </span>
            <div key={idx} className={`${msg.sender === "You" ? "sent" : "received"}`}>
              <p>{msg.text}</p>
            </div>
          </>
        ))}

        {/* 👇 invisible anchor to auto-scroll */}
        <div ref={chatEndRef} />
      </div>

      <div className="chat-input">
        <span className="add-files"><IoMdAdd /></span>
        <div className="chat-input-emoji-input">
          <span className="emoji-button" onClick={(e) => {
            e.stopPropagation();
            setEmojiPickerVisible((prev) => !prev);
          }}>
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
