import { createContext, useState } from "react";

export const ModalContext = createContext();

export default function ModalProvider({ children }) {
  const [inviteModalOpen, setInviteModalOpen] = useState(false);
  const [inviteEmail, setInviteEmail] = useState("");
  const [imageModalOpen, setImageModalOpen] = useState(false);
  const [imageSrc, setImageSrc] = useState(null);
  const [imgUsername, setImgUsername] = useState("");

  const openInviteModal = (email = "") => {
    setInviteEmail(email);
    setInviteModalOpen(true);
  };

  const closeInviteModal = () => {
    setInviteModalOpen(false);
    setInviteEmail("");
  };

   const openImageModal = (src, name) => {
    setImageSrc(src);
    setImgUsername(name);
    setImageModalOpen(true);
  };

  const closeImageModal = () => {
    setImageModalOpen(false);
    setImgUsername("");
    setImageSrc(null);
  };

  return (
    <ModalContext.Provider
      value={{
        inviteModalOpen,
        inviteEmail,
        openInviteModal,
        closeInviteModal,
        imageSrc,
        imgUsername,
        imageModalOpen,
        openImageModal,
        closeImageModal
      }}
    >
      {children}
    </ModalContext.Provider>
  );
}
