import React, { createContext, useState } from "react";

export const ChatContext = createContext();

export function ChatProvider({ children }) {
  const [selectedContact, setSelectedContact] = useState(null);

  // New states for search
  const [searchQuery, setSearchQuery] = useState("");       
  const [searchResults, setSearchResults] = useState([]);

  return (
    <ChatContext.Provider
      value={{
        selectedContact,
        setSelectedContact,
        searchQuery,
        setSearchQuery,
        searchResults,
        setSearchResults,
      }}
    >
      {children}
    </ChatContext.Provider>
  );
}
