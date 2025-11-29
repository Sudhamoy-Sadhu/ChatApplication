import React, { useContext, useState, useEffect } from "react";
import axios from "axios";
import "./ChatList.css";
import { ChatContext } from "../ContextAPI/ChatContext";
import { ModalContext } from "../ContextAPI/ModalContext";

export default function ChatList() {
  const { selectedContact, setSelectedContact, searchResults, searchQuery } =
    useContext(ChatContext);
  const { openInviteModal } = useContext(ModalContext);
  const [contacts, setContacts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Fetch logged-in user's contacts
  useEffect(() => {
    const fetchContacts = async () => {
      try {
        setLoading(true);
        const response = await axios.get(
          "http://localhost:8080/contacts/allContacts",
          { withCredentials: true }
        );
        setContacts(response.data);
      } catch (err) {
        setError("Unable to load contacts");
      } finally {
        setLoading(false);
      }
    };
    fetchContacts();
  }, []);

  // Send friend request
  const sendRequest = async (userId) => {
    try {
      await axios.post(
        "http://localhost:8080/contacts/request",
        { userId },
        { withCredentials: true }
      );
      alert("Connection request sent!");
    } catch (err) {
      console.error(err);
    }
  };

  // Send email invite
  const sendInvite = async (email) => {
    try {
      await axios.post(
        "http://localhost:8080/invite",
        { email },
        { withCredentials: true }
      );
      alert("Invitation sent!");
    } catch (err) {
      console.error(err);
    }
  };

  if (loading)
    return <div className="chat-list-loading">Loading contacts...</div>;
  if (error) return <div className="chat-list-error">{error}</div>;

  // If searching, show search results. Otherwise contacts.
  const displayList = searchQuery ? searchResults : contacts;

  if (!displayList || displayList.length === 0) {
    return (
      <div className="empty-chat-list">
        <h3>
          {searchQuery ? "No users found!" : "Start connecting with friends!"}
        </h3>
        <p>
          {searchQuery
            ? "Try different username or email."
            : "Search by email to find people or invite new users."}
        </p>
      </div>
    );
  }

  return (
    <div className="chat-list">
      {displayList.map((user, i) => {
        const isNotFound = user.exists === false;
        const isContact =
          !isNotFound && contacts.some((c) => c.email === user.email);

        return (
          <div
            key={i}
            className="chat-item"
            onClick={() => !isNotFound && isContact && setSelectedContact(user)}
          >
            <div className="avatar">
              <img
                src={user.profilePicture || "/default-avatar.png"}
                alt="profile"
              />
            </div>

            <div className="chat-info">
              <div className="details">
                {/* ⭐ Case 1: USER NOT FOUND */}
                {isNotFound ? (
                  <>
                    <h4>User not found</h4>
                    <p>{user.search}</p>
                  </>
                ) : (
                  <>
                    {/* ⭐ Case 2: USER FOUND */}
                    <h4>{user.username}</h4>
                    <p>{isContact ? "Already connected" : user.email}</p>
                  </>
                )}
              </div>

              {/* ⭐ BUTTON LOGIC */}
              {searchQuery &&
                (isNotFound ? (
                  <button
                    className="invite-btn"
                    onClick={() => openInviteModal(user.search)}
                  >
                    Invite
                  </button>
                ) : !isContact ? (
                  <button
                    className="connect-btn"
                    onClick={() => sendRequest(user.id)}
                  >
                    Connect
                  </button>
                ) : null)}
            </div>
          </div>
        );
      })}
    </div>
  );
}
