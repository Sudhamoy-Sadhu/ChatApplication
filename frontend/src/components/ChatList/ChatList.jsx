import React, { useContext, useState, useEffect } from "react";
import axios from "axios";
import "./ChatList.css";
import { ChatContext } from "../ContextAPI/ChatContext";

export default function ChatList() {
  const {
    selectedContact,
    setSelectedContact,
    searchResults,
    searchQuery
  } = useContext(ChatContext);

  const [contacts, setContacts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Fetch user contacts
  useEffect(() => {
    const fetchContacts = async () => {
      try {
        setLoading(true);
        setError(null);
        const response = await axios.get("http://localhost:8080/contacts/allContacts");
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

  // Handle connection / invite
  const sendRequest = async (userId) => {
    try {
      await axios.post(`http://localhost:8080/contacts/request`, { userId });
      alert("Connection request sent!");
    } catch (err) {
      if (err.response?.status === 404) {
        await axios.post(`http://localhost:8080/invite`, { userId });
        alert("Invitation sent via email!");
      } else {
        console.error(err);
      }
    }
  };

  if (loading) return <div className="chat-list-loading">Loading contacts...</div>;
  if (error) return <div className="chat-list-error">{error}</div>;

  // Decide which list to show: search results or contacts
  const displayList = searchQuery ? searchResults : contacts;

  if (!displayList || displayList.length === 0) {
    return (
      <div className="empty-chat-list">
        <h3>{searchQuery ? "No users found!" : "Start connecting with friends!"}</h3>
        <p>{searchQuery ? "Try searching with a different name or email." : "Find people to chat with and build your circle."}</p>
        <p>{searchQuery ? "" : "Search with email for existing users or sent an invite!👋"}</p>
      </div>
    );
  }

  return (
    <div className="chat-list">
      {displayList.map((user) => {
        // Check if this user is already a contact
        const isContact = contacts.some((c) => c.id === user.id);

        return (
          <div key={user.id} className="chat-item" onClick={() => isContact && setSelectedContact(user)}>
            <div className="avatar">
              <img src={user.profileImageUrl || "/default-avatar.png"} alt={user.username || user.name} />
            </div>
            <div className="chat-info">
              <div className="details">
                <h4>{user.username || user.name}</h4>
                <p>{isContact ? user.lastMessage || "Say hi!" : user.email}</p>
              </div>
              {!isContact && (
                <button className="connect-btn" onClick={() => sendRequest(user.id)}>
                  {user.exists ? "Connect" : "Invite"}
                </button>
              )}
              {isContact && (
                <span>
                  {user.lastMessageTime
                    ? new Date(user.lastMessageTime).toLocaleTimeString("en-GB", { hour: "2-digit", minute: "2-digit" })
                    : ""}
                </span>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
}
