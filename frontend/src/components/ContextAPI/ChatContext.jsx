import React, { createContext, useState, useEffect } from "react";

export const ChatContext = createContext();

export function ChatProvider({ children }) {
  const [selectedContact, setSelectedContact] = useState(null);

  // New states for search
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [contacts, setContacts] = useState([]);


  useEffect(() => {
    if (!selectedContact) return;

    const updated = contacts.find(
      c => c.roomId === selectedContact.roomId
    );

    if (!updated) return;

    setSelectedContact(prev => ({
      ...prev,
      status: updated.status,
      lastMessage: updated.lastMessage,
      lastMessageTime: updated.lastMessageTime,
      unreadCount: updated.unreadCount
    }));
  }, [contacts]);


  return (
    <ChatContext.Provider
      value={{
        selectedContact,
        setSelectedContact,
        contacts,
        setContacts,
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
