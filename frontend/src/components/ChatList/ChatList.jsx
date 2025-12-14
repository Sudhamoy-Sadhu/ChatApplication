import React, { useContext, useState, useEffect, useRef } from "react";
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

        const sorted = response.data.map(c => ({
          ...c,
          unreadCount: c.unreadCount ?? 0
        })).sort((a, b) => {
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

  const selectedRoomRef = useRef(null);
  useEffect(() => {
    selectedRoomRef.current = selectedContact?.roomId ?? null;
  }, [selectedContact]);


  // ░░ WEBSOCKET LISTENER ░░
  const chatlistSubRef = useRef(null);
  const unreadSubRef = useRef(null);

  useEffect(() => {
    if (!client || !connected || !client.connected || !userLoggedInId) return;

    // cleanup old subscriptions
    chatlistSubRef.current?.unsubscribe();
    unreadSubRef.current?.unsubscribe();

    // ===============================
    // Chatlist subscription
    // ===============================
    chatlistSubRef.current = client.subscribe(
      `/topic/chatlist/${userLoggedInId}`,
      (msg) => {
        const data = JSON.parse(msg.body);

        if (data.type === "STATUS_CHANGE") {
          const { userId, status } = data;

          setContacts(prev =>
            prev.map(c =>
              c.userId === userId ? { ...c, status } : c
            )
          );

          setSelectedContact(prev =>
            prev?.userId === userId
              ? { ...prev, status }
              : prev
          );
        }

        if (data.type === "LAST_MESSAGE") {
          const { roomId, msg: lastMessage, time } = data;

          setContacts(prev => {
            const updated = prev.map(c =>
              c.roomId === roomId
                ? { ...c, lastMessage, lastMessageTime: time }
                : c
            );

            return updated.sort((a, b) => {
              const t1 = a.lastMessageTime ? new Date(a.lastMessageTime).getTime() : 0;
              const t2 = b.lastMessageTime ? new Date(b.lastMessageTime).getTime() : 0;
              return t2 - t1;
            });
          });

          setSelectedContact(prev =>
            prev?.roomId === roomId
              ? { ...prev, lastMessage, lastMessageTime: time }
              : prev
          );
        }
      }
    );

    // ===============================
    // Unread count subscription
    // ===============================
    unreadSubRef.current = client.subscribe(
      "/user/queue/unread",
      async (msg) => {
        const { roomId, unreadCount } = JSON.parse(msg.body);

        // 🔑 If this room is currently open, ignore unread
        if (selectedRoomRef.current === roomId) return;

        // Otherwise update unread count
        setContacts(prev =>
          prev.map(c =>
            c.roomId === roomId
              ? { ...c, unreadCount }
              : c
          )
        );
      }
    );


    // cleanup on unmount / reconnect
    return () => {
      chatlistSubRef.current?.unsubscribe();
      unreadSubRef.current?.unsubscribe();
    };

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
        const lastMessage = user.lastMessage || "Start your conversation!";
        const lastMessageTime = user.lastMessageTime || "";
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
              setContacts(prev =>
                prev.map(c =>
                  c.roomId === roomId
                    ? { ...c, unreadCount: 0 }
                    : c
                )
              );

              if (!roomId || isNotFound || !isContact) return;

              if (!isNotFound && isContact) {

                const fullContact = contacts.find(c => c.email === user.email);

                if (!fullContact) return;

                setSelectedContact({
                  userId: user.id,
                  username,
                  profileImageUrl: user.profileImageUrl ? user.profileImageUrl : "/assets/default-logo.png",
                  email,
                  roomId,
                  roomName: user.roomName ?? user.username,
                  status,
                  lastMessage,
                  lastMessageTime
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
                      <p className="last-message">{lastMessage}</p>
                    </div>
                    <div className="chat-time">{lastMessageTime}
                      {user.unreadCount > 0 && (
                        <span className="unread-badge">
                          {user.unreadCount}
                        </span>
                      )}
                    </div>
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
