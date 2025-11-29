import React, { useState } from "react";
import "./InviteModal.css";
import axios from "axios";
import { toast } from "react-toastify";

export default function InviteModal({ onClose, onSend }) {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");

  const handleBackgroundClick = (e) => {
    if (e.target.className === "invite-modal-overlay") {
      onClose();
    }
  };

  // Send email invite
  const sendInvite = async () => {
    try {
      await axios.post(
        "http://localhost:8080/invitations/sendInvite",
        { name, email },
        { withCredentials: true }
      );

      toast.success("Invitation sent!", { autoClose: 1500 });
      setTimeout(() => {
      onClose();  
      }, 1600);
    } catch (err) {
      console.error(err);
      toast.error(err.response?.data || "Failed to send invite");
    }
  };

  return (
    <div className="invite-modal-overlay" onClick={handleBackgroundClick}>
      <div className="invite-modal-container">
        <button className="close-btn" onClick={onClose}>
          ✕
        </button>

        <h2>Send Invite</h2>

        <div className="modal-field">
          <label>Enter Name</label>
          <input
            type="text"
            placeholder="Full Name"
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
        </div>

        <div className="modal-field">
          <label>Enter Email</label>
          <input
            type="email"
            placeholder="Email Address"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>

        <button className="send-btn" onClick={sendInvite}>
          Send Invite
        </button>
      </div>
    </div>
  );
}
