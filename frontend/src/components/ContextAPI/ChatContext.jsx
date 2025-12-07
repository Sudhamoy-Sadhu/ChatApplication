import React, { createContext, useState, useEffect } from "react";

export const ChatContext = createContext();

export function ChatProvider({ children }) {
  const [selectedContact, setSelectedContact] = useState(null);

  // New states for search
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [contacts, setContacts] = useState([]);


  useEffect(() => {
    if (selectedContact && contacts.length > 0) {
      const updated = contacts.find(c => c.id === selectedContact.id);
      if (updated) setSelectedContact(updated);
    }
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
