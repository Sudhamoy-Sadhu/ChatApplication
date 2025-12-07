import React, { useContext, useState, useEffect } from "react";
import axios from "axios";
import "./ChatList.css";
import { ChatContext } from "../ContextAPI/ChatContext";
import { ModalContext } from "../ContextAPI/ModalContext";
import { AuthContext } from "../ContextAPI/AuthContext";
import { usePageManager } from "../ContextAPI/PageManagerContext";

export default function ChatList() {
  const { contacts, setContacts, selectedContact, setSelectedContact, searchResults, searchQuery } =
    useContext(ChatContext);

  const { openInviteModal } = useContext(ModalContext);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [sentRequests, setSentRequests] = useState(new Set());
  const { user } = useContext(AuthContext);
   const { goToPage } = usePageManager();
  const userLoggedInId = user?.id;

  // Fetch logged-in user's contacts
  useEffect(() => {
    const fetchContacts = async () => {
      try {
        setLoading(true);
        const response = await axios.get(
          "http://localhost:8080/contacts/allContacts",
          { withCredentials: true }
        );

        // Sort by recent message time
        const sorted = response.data.sort((a, b) => {
          const t1 = a.lastMessageTime ? new Date(a.lastMessageTime).getTime() : 0;
          const t2 = b.lastMessageTime ? new Date(b.lastMessageTime).getTime() : 0;
          return t2 - t1;
        });

        setContacts(sorted);

      } catch (err) {
        setError("Unable to load contacts");
      } finally {
        setLoading(false);
      }
    };
    fetchContacts();
  }, []);

  // Send connection request
  const sendRequest = async (targetId) => {
    try {
      await axios.post(
        `http://localhost:8080/connection/sendRequest/${targetId}`,
        {},
        { withCredentials: true }
      );
      setSentRequests((prev) => new Set(prev).add(targetId));
      alert("Connection request sent!");
    } catch (err) {
      alert(err.response.data);
      console.error(err);
    }
  };

  // Check pending statuses
  useEffect(() => {
    if (!userLoggedInId || !searchResults || searchResults.length === 0) return;

    const checkStatuses = async () => {
      const updatedSet = new Set(sentRequests);

      for (const u of searchResults) {
        if (!u.id) continue;

        try {
          const response = await axios.get(
            `http://localhost:8080/connection/status/${userLoggedInId}/${u.id}`,
            { withCredentials: true }
          );

          if (response.data === "PENDING") updatedSet.add(u.id);

        } catch (err) {
          console.error("Status check failed for user:", u.id, err);
        }
      }

      setSentRequests(updatedSet);
    };

    checkStatuses();
  }, [userLoggedInId, searchResults]);

  if (loading) return <div className="chat-list-loading">Loading contacts...</div>;
  if (error) return <div className="chat-list-error">{error}</div>;

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
        const username = user.username || user.name;
        const email = user.email;
        const avatar = user.profileImageUrl || "/assets/default-logo.png";
        const lastMsg = user.lastMessage || "Start your conversation!";
        const lastMsgTime = user.lastMessageTime || "";
        const status = user.status || false;
        const roomId = user.roomId;
        const roomName = user.roomName || username;

        const isContact =
          !isNotFound &&
          contacts.some((c) => c.email === user.email);

        return (
          <div
            key={i}
            className="chat-item"
            onClick={() =>
              !isNotFound && isContact && setSelectedContact({
                id: user.id,
                name: username,
                email,
                pic: avatar,
                roomId,
                roomName,
                online: status
                && goToPage("home")
              })
            }
          >
            {/* Avatar */}
            <div className="avatar">
              <img src={avatar} alt="profile" />
            </div>

            {/* Chat Info */}
            <div className="chat-info">
              <div className="details">
                {/* Case 1: USER NOT FOUND */}
                {isNotFound ? (
                  <>
                    <h4>User not found</h4>
                    <p>{user.search}</p>
                  </>
                ) : (
                  <>
                    {/* Case 2: USER FOUND / CONTACT */}
                    <div className="details-name">
                      <h4>{roomName}</h4>
                      <p className="last-message">{lastMsg}</p>
                    </div>
                    <div className="chat-time">{lastMsgTime}</div>
                  </>
                )}
              </div>

              {/* BUTTON LOGIC */}
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
                    className={
                      sentRequests.has(user.id)
                        ? "connect-btn-sent"
                        : "connect-btn"
                    }
                    disabled={sentRequests.has(user.id)}
                    onClick={() => sendRequest(user.id)}
                  >
                    {sentRequests.has(user.id) ? "Pending" : "Connect"}
                  </button>
                ) : null)}
            </div>
          </div>
        );
      })}
    </div>
  );
}
