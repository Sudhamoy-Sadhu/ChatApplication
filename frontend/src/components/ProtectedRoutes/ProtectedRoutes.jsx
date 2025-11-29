import React, { useContext, useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import Cookies from "js-cookie";
import { AuthContext } from "../ContextAPI/AuthContext";
import { jwtDecode } from "jwt-decode";

export default function ProtectedRoutes({ children }) {
  const { token, setToken, logout } = useContext(AuthContext);
  const [loading, setLoading] = useState(true);
  const [isAuth, setIsAuth] = useState(false);

  useEffect(() => {
    const checkAuth = async () => {
      const accessToken = Cookies.get("access_token");
      if (!accessToken) {
        setIsAuth(false);
        setLoading(false);
        return;
      }

      try {
        const decoded = jwtDecode(accessToken);
        const isExpired = decoded.exp * 1000 < Date.now();
        if (isExpired) {
          logout();
          setIsAuth(false);
          setLoading(false);
          return;
        }

        // Optional: Backend validation
        const validatedOnce = Cookies.get("validatedOnce");
        if (!validatedOnce) {
          const res = await fetch("http://localhost:8080/auth/validate", {
            headers: { Authorization: `Bearer ${accessToken}` },
          });

          if (!res.ok) {
            logout();
            setIsAuth(false);
            setLoading(false);
            return;
          }

          Cookies.set("validatedOnce", "true", { expires: 1 });
        }

        setIsAuth(true);
      } catch (err) {
        console.error("Token error:", err);
        logout();
        setIsAuth(false);
      } finally {
        setLoading(false);
      }
    };

    checkAuth();
  }, [token, logout]);

  if (loading) return <div>Loading...</div>;

  return isAuth ? children : <Navigate to="/login" replace />;
}
