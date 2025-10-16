import React, { useContext, useState, useEffect } from "react";
import axios from "axios";
import "./ChatList.css";
import { ChatContext } from "../ContextAPI/ChatContext";

export default function ChatList() {
  const { setSelectedContact } = useContext(ChatContext);
  const [contacts, setContacts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    const fetchContacts = async () => {
      try {
        setLoading(true);
        setError(null);
        const response = await axios.get("http://localhost:8080/api/contacts");
        setContacts(response.data);
      } catch (err) {
        console.error("Failed to fetch contacts:", err);
        setError("Unable to load contacts");
      } finally {
        setLoading(false);
      }
    };

    fetchContacts();
  }, []);

  if (loading) return <div className="chat-list-loading">Loading contacts...</div>;
  if (error) return <div className="chat-list-error">{error}</div>;

  if (!contacts || contacts.length === 0) {
    return (
      <div className="empty-chat-list">
        <h3>Start connecting with friends and family 👋</h3>
        <p>Find people to chat with and build your circle.</p>
        <button className="find-people-btn" onClick={() => setShowModal(true)}>
          Find People
        </button>

        {showModal && (
          <div className="modal-overlay" onClick={() => setShowModal(false)}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
              <h3>Find People</h3>
              <input
                type="text"
                placeholder="Search by name or email..."
                className="search-input"
              />
              <div className="modal-actions">
                <button onClick={() => setShowModal(false)} className="close-btn">
                  Close
                </button>
                <button className="search-btn">Search</button>
              </div>
            </div>
          </div>
        )}
      </div>
    );
  }

  return (
    <div className="chat-list">
      {contacts.map((c) => (
        <div key={c.id} className="chat-item" onClick={() => setSelectedContact(c)}>
          <div className="avatar">
            <img src={c.pic || "/default-avatar.png"} alt={c.name} />
          </div>
          <div className="chat-info">
            <div className="details">
              <h4>{c.name}</h4>
              <p>{c.lastMessage}</p>
            </div>
            <span>
              {new Date(c.lastMessageTime).toLocaleTimeString("en-GB", {
                hour: "2-digit",
                minute: "2-digit",
              })}
            </span>
          </div>
        </div>
      ))}
    </div>
  );
}
