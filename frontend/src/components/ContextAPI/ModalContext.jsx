import { createContext, useState } from "react";

export const ModalContext = createContext();

export default function ModalProvider({ children }) {
  const [inviteModalOpen, setInviteModalOpen] = useState(false);
  const [inviteEmail, setInviteEmail] = useState("");

  const openInviteModal = (email = "") => {
    setInviteEmail(email);
    setInviteModalOpen(true);
  };

  const closeInviteModal = () => {
    setInviteModalOpen(false);
    setInviteEmail("");
  };

  return (
    <ModalContext.Provider
      value={{
        inviteModalOpen,
        inviteEmail,
        openInviteModal,
        closeInviteModal,
      }}
    >
      {children}
    </ModalContext.Provider>
  );
}
