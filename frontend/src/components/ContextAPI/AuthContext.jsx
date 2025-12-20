import React, { createContext, useState, useEffect } from "react";
import axios from "axios";
import Cookies from "js-cookie";

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [userId, setUserId] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  // ================================
  // Axios interceptor for 401 errors
  // ================================
  useEffect(() => {
    const interceptor = axios.interceptors.response.use(
      (res) => res,
      async (error) => {
        const originalRequest = error.config;

        if (!originalRequest || !originalRequest.url) return Promise.reject(error);

        // Only retry if user is logged in
        if (!isAuthenticated) return Promise.reject(error);

        // Avoid infinite loop
        if (error.response?.status === 401 && !originalRequest._retry && !originalRequest.url.includes("/auth/refresh")) {
          originalRequest._retry = true;
          try {
            await axios.post("http://localhost:8080/auth/refresh", {}, { withCredentials: true });
            return axios(originalRequest);
          } catch (_) {
            await handleLogout();
            return Promise.reject(error);
          }
        }

        return Promise.reject(error);
      }
    );

    return () => axios.interceptors.response.eject(interceptor);
  }, [isAuthenticated]);

  // ================================
  // Initialize auth on app load
  // ================================
  useEffect(() => {
    const initializeAuth = async () => {
      setLoading(true);
      const cookieData = Cookies.get("userData");

      if (!cookieData) {
        setIsAuthenticated(false);
        setLoading(false);
        return;
      }

      try {
        // Try to refresh access_token if expired
        await axios.post("http://localhost:8080/auth/refresh", {}, { withCredentials: true });
        const response = await axios.get("http://localhost:8080/users/profile-data", { withCredentials: true});

        const userData = response.data;
        if (userData.profilePicture) {
          userData.profilePicture = `data:image/jpeg;base64,${userData.profilePicture}`;
        }
        console.log(userData.profilePicture);

        setUser(userData);
        setUserId(userData.id);
        setIsAuthenticated(true);
      } catch (_) {
        // Refresh failed → logout
        console.error("Session restoration failed");
        await handleLogout();
      } finally {
        setLoading(false);
      }
    };

    initializeAuth();
  }, []);

  // ================================
  // Login
  // ================================
  const login = (userData) => {
    const formattedPicture = userData.profilePicture 
      ? `data:image/jpeg;base64,${userData.profilePicture}` 
      : null;

    const normalizedUser = {
      id: Number(userData.id),
      username: userData.username,
      email: userData.email,
      profilePicture: formattedPicture,
      status: userData.status,
    };

    const { profilePicture, ...cookieData } = normalizedUser;
    Cookies.set("userData", JSON.stringify(cookieData), { expires: 1 });
    setUser(normalizedUser);
    setIsAuthenticated(true);
  };


  // ================================
  // Logout
  // ================================
  const handleLogout = async () => {
    try {
      await axios.post("http://localhost:8080/auth/logout", {}, { withCredentials: true });
    } catch (_) { }
    Cookies.remove("userData");
    setUser(null);
    setUserId(null);
    setIsAuthenticated(false);
  };

  return (
    <AuthContext.Provider value={{ user, setUser, isAuthenticated, login, logout: handleLogout, loading }}>
      {children}
    </AuthContext.Provider>
  );
};
