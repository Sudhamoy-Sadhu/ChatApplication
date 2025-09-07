import React, { useState } from "react";
import './ChatHome.css';
import Sidebar from "../Sidebar/Sidebar.jsx"
import ChatList from "../ChatList/ChatList.jsx"
import ChatWindow from "../ChatWindow/ChatWindow.jsx"



export default function ChatHome() {
    const [selectedContact, setSelectedContact] = useState("");
    return (
        <>
            <div className="Chat-home-main">
                <div className="sidebar">
                    <Sidebar />
                    <ChatList />
                </div>
                <div className="chat-window">
                    <ChatWindow />
                </div>
            </div>
        </>
    )
}