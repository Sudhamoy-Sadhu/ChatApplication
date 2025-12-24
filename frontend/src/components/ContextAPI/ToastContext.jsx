// ContextAPI/ToastContext.jsx
import React, { createContext, useContext } from 'react';
import { toast } from 'react-toastify';

const ToastContext = createContext();

export const ToastProvider = ({ children }) => {
    const showToast = {
        success: (msg) => toast.success(msg),
        error: (msg) => toast.error(msg),
        info: (msg) => toast.info(msg),
        warn: (msg) => toast.warn(msg)
    };

    return (
        <ToastContext.Provider value={{ showToast }}>
            {children}
        </ToastContext.Provider>
    );
};

export const useToast = () => useContext(ToastContext);