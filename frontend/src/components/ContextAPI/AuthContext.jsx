import React, { createContext, useState, useEffect } from "react";
import Cookies from "js-cookie";

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(null);

  useEffect(() => {
    const accessToken = Cookies.get("access_token");
    const userData = Cookies.get("userData");

    if (accessToken) {
      setToken(accessToken);
      setUser(userData ? JSON.parse(userData) : null);
      setIsAuthenticated(true);
    } else {
      setIsAuthenticated(false);
    }
  }, []);

  // 🔥 LOGIN — now only accessToken + userData
  const login = (accessToken, userData) => {
    Cookies.set("access_token", accessToken, { expires: 1, secure: false });
    Cookies.set("userData", JSON.stringify(userData), {
      expires: 1,
      secure: false,
    });

    setToken(accessToken);
    setUser(userData);
    setIsAuthenticated(true);
  };

  // 🔥 LOGOUT
  const logout = () => {
    Cookies.remove("access_token");
    Cookies.remove("userData");

    // refresh_token cookie is HttpOnly; backend will clear it during /logout
    setToken(null);
    setUser(null);
    setIsAuthenticated(false);
  };

  return (
    <AuthContext.Provider
      value={{ token, isAuthenticated, user, login, logout }}
    >
      {children}
    </AuthContext.Provider>
  );
};
