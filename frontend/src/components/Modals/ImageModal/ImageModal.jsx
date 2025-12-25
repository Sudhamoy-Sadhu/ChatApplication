import React, { useContext, useState, useEffect } from "react";
import { ModalContext } from "../../ContextAPI/ModalContext";
import "./ImageModal.css";
import { IoIosCloseCircle } from "react-icons/io";

export default function ImageModal() {
    const { imageModalOpen, imageSrc, imgUsername, closeImageModal } = useContext(ModalContext);
    const [zoomed, setZoomed] = useState(false);

    useEffect(() => {
        const esc = (e) => e.key === "Escape" && closeImageModal();
        window.addEventListener("keydown", esc);
        return () => window.removeEventListener("keydown", esc);
    }, []);

    useEffect(() => {
        if (!imageModalOpen) setZoomed(false);
    }, [imageModalOpen]);


    if (!imageModalOpen || !imageSrc) return null;

    return (
        <>
            <div className="image-modal-main" onClick={closeImageModal}>
                <div className="image-modal-body" onClick={(e) => e.stopPropagation()}>
                    <div className="image-modal-close">
                        <h4>{imgUsername}</h4>
                        <button onClick={closeImageModal}><IoIosCloseCircle /></button>
                    </div>
                    <img src={imageSrc} alt="DP" className={`preview-image ${zoomed ? "zoomed" : ""}`} onClick={() => setZoomed(!zoomed)} />
                </div>
            </div>
        </>
    )
}