import React, { createContext, useState } from "react";

export const ChatContext = createContext();

export function ChatProvider({ children }) {
  const [selectedContact, setSelectedContact] = useState(null);

  return (
    <ChatContext.Provider value={{ selectedContact, setSelectedContact }}>
      {children}
    </ChatContext.Provider>
  );
}
