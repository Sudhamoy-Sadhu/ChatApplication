import React, { useContext, useState, useEffect } from "react";
import axios from "axios";
import "./ChatList.css";
import { ChatContext } from "../ContextAPI/ChatContext";
import { ModalContext } from "../ContextAPI/ModalContext";
import { AuthContext } from "../ContextAPI/AuthContext";
import { usePageManager } from "../ContextAPI/PageManagerContext";
import { SocketContext } from "../ContextAPI/SocketContext";

export default function ChatList() {
  const { contacts, setContacts, selectedContact, setSelectedContact, searchResults, searchQuery } =
    useContext(ChatContext);

  const { client, connected } = useContext(SocketContext);
  const { openInviteModal } = useContext(ModalContext);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [sentRequests, setSentRequests] = useState(new Set());
  const { user } = useContext(AuthContext);
  const { goToPage } = usePageManager();

  const userLoggedInId = user?.id;


  // ░░ FETCH CONTACTS ░░
  useEffect(() => {
    const fetchContacts = async () => {
      try {
        setLoading(true);
        const response = await axios.get(
          "http://localhost:8080/contacts/allContacts",
          { withCredentials: true }
        );

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

  // ░░ WEBSOCKET LISTENER ░░
  useEffect(() => {
    if (!client || !connected) return;

    const sub = client.subscribe(`/topic/chatlist/${userLoggedInId}`, (msg) => {
      const data = JSON.parse(msg.body);

      if (data.type === "STATUS_CHANGE") {
        const { userId, status } = data;

        setContacts(prev =>
          prev.map(c =>
            c.id === userId ? { ...c, status } : c
          )
        );

        setSelectedContact(prev =>
          prev?.id === userId
            ? { ...prev, status }
            : prev
        );
      }


      if (data.type === "LAST_MESSAGE") {
        const { roomId, msg, time } = data;

        // update chatlist
        setContacts(prev =>
          prev.map(c =>
            c.roomId === roomId
              ? { ...c, lastMessage: msg, lastMessageTime: time }
              : c
          )
        );

        // update chat window (BUT DO NOT TOUCH name or pic)
        setSelectedContact(prev =>
          prev?.roomId === roomId
            ? { ...prev, lastMessage: msg, lastMessageTime: time }
            : prev
        );
      }

    });

    return () => sub.unsubscribe();
  }, [client, connected, userLoggedInId]);

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

  if (loading) return <div className="chat-list-loading">Loading...</div>;
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
        const username = user.username;
        const email = user.email;
        const profileImageUrl = user.profileImageUrl || "/assets/default-logo.png";
        const lastMsg = user.lastMessage || "Start your conversation!";
        const lastMsgTime = user.lastMessageTime || "";
        const status = user.status ?? "INACTIVE";
        const roomId = user.roomId;

        const isContact =
          !isNotFound &&
          contacts.some((c) => c.email === user.email);

        return (
          <div
            key={i}
            className="chat-item"
            onClick={() => {
              if (!isNotFound && isContact) {

                const fullContact = contacts.find(c => c.email === user.email);

                if (!fullContact) return;

                setSelectedContact({
                  id: user.id,
                  username,
                  profileImageUrl: user.profileImageUrl ? user.profileImageUrl : "/assets/default-logo.png",
                  email,
                  roomId,
                  roomName: user.roomName ?? user.username,
                  status,
                  lastMsg,
                  lastMsgTime
                });
                goToPage("home");
              }
            }}
          >
            <div className="avatar">
              <img src={profileImageUrl} alt="profile-dp" />
            </div>

            <div className="chat-info">
              <div className="details">
                {isNotFound ? (
                  <>
                    <h4>User not found</h4>
                    <p>{user.search}</p>
                  </>
                ) : (
                  <>
                    <div className="details-name">
                      <h4>{user.roomName ?? username}</h4>
                      <p className="last-message">{lastMsg}</p>
                    </div>
                    <div className="chat-time">{lastMsgTime}</div>
                  </>
                )}
              </div>
              {searchQuery &&
                (isNotFound ? (
                  <button
                    className="invite-btn"
                    onClick={(e) => { e.stopPropagation(); openInviteModal(user.search) }}
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
                    onClick={(e) => { e.stopPropagation(); sendRequest(user.id) }}
                  >
                    {sentRequests.has(user.id) ? "Pending" : "Connect"}
                  </button>
                ) : null)}
            </div>
          </div>
        );
      })}
    </div >
  );
}
