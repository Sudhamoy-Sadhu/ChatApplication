import React, { useContext, useState } from "react";
import "./ChatList.css";
import { ChatContext } from "../ContextAPI/ChatContext";

const dummyContacts = [
  { id: 1, name: "Alex", lastMessage: "Hello!", pic: "../assets/Logo-M.jpg", online: true, time: new Date().toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' }) },
  { id: 2, name: "Jasmine", lastMessage: "How are you?", pic: "../assets/Logo-F.webp", online: false, time: new Date().toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' }) },
  { id: 3, name: "Alisha", lastMessage: "See you!", pic: "../assets/Logo-F2.jpg", online: true, time: new Date().toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' }) }
];

export default function ChatList() {
  const { setSelectedContact } = useContext(ChatContext);
  // const [activeItem, setActiveItem] = useState(null);

  // function onClickContact(id) {
  //   setActiveItem(id);
  // }

  return (
    <div className="chat-list">
      {dummyContacts.map((c) => (
        <div key={c.id} className="chat-item" onClick={() => setSelectedContact(c)}>
          <div className="avatar"><img src={c.pic} alt="Dp" /></div>
          <div className="chat-info">
            <div className="details">
              <h4>{c.name}</h4>
              <p>{c.lastMessage}</p>
            </div>
            <span>{c.time}</span>
          </div>
        </div>
      ))}
    </div>
  );
}
