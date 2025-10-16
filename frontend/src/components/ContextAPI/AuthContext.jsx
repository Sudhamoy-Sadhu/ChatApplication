import React, { createContext, useState, useEffect } from "react";
import Cookies from "js-cookie";

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(null);

  useEffect(() => {
    const accessToken = Cookies.get("accessToken");
    const userData = Cookies.get("userData");
    if (accessToken) {
      setToken(accessToken);
      setIsAuthenticated(true);
      setUser(userData ? JSON.parse(userData) : null);
    } else {
      setIsAuthenticated(false);
    }
  }, []);

  const login = (accessToken, refreshToken, userData) => {
    Cookies.set("accessToken", accessToken, { expires: 1, secure: false });
    Cookies.set("refreshToken", refreshToken, { expires: 7, secure: false });
    Cookies.set("userData", JSON.stringify(userData), { expires: 1, secure: false });
    setToken(accessToken);
    setUser(userData);
    setIsAuthenticated(true);
  };

  const logout = () => {
    Cookies.remove("accessToken");
    Cookies.remove("refreshToken");
    Cookies.remove("userData");
    setUser(null);
    setToken(null);
    setIsAuthenticated(false);
  };

  return (
    <AuthContext.Provider value={{ token, isAuthenticated, user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
