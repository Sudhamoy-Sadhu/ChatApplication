import React, { useContext } from "react";
import "./ChatHome.css";
import Sidebar from "../Sidebar/Sidebar.jsx";
import ChatList from "../ChatList/ChatList.jsx";
import ChatWindow from "../ChatWindow/ChatWindow.jsx";
import { ModalContext } from "../ContextAPI/ModalContext.jsx";
import InviteModal from "../Modals/InviteModal/InviteModal.jsx";
import { ToastContainer } from "react-toastify";
import { usePageManager } from "../ContextAPI/PageManagerContext.jsx";
import ConnectionRequest from "../ConnectionRequest/ConnectionRequest.jsx";
import Profile from "../Profile/Profile.jsx"


export default function ChatHome() {
  const { inviteModalOpen, inviteEmail, closeInviteModal } =
    useContext(ModalContext);
  const { activePage } = usePageManager();
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
          {activePage ==="request" && <ConnectionRequest />}
          {activePage ==="setting" && <ConnectionRequest />}
          {activePage ==="profile" && <Profile />}
          {activePage ==="home" && <ChatWindow/>}
        </div>
      </div>
      <ToastContainer position="top-right" autoClose={1500} theme="light" />
    </>
  );
}
