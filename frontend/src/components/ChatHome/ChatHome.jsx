import React, { useContext, useState } from "react";
import "./ChatHome.css";
import Sidebar from "../Sidebar/Sidebar.jsx";
import ChatList from "../ChatList/ChatList.jsx";
import ChatWindow from "../ChatWindow/ChatWindow.jsx";
import { ModalContext } from "../ContextAPI/ModalContext.jsx";
import InviteModal from "../Modals/InviteModal/InviteModal.jsx";
import { ToastContainer } from "react-toastify";

export default function ChatHome() {
  const { inviteModalOpen, inviteEmail, closeInviteModal } =
    useContext(ModalContext);
  return (
    <>
      {inviteModalOpen && (
        <InviteModal email={inviteEmail} onClose={closeInviteModal} />
      )}
      <div className="Chat-home-main">
        <div className="sidebar">
          <Sidebar />
          <ChatList />
        </div>
        <div className="chat-window">
          <ChatWindow />
        </div>
      </div>
      <ToastContainer position="top-right" autoClose={2000} theme="light" />
    </>
  );
}
