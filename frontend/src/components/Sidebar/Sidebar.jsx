import React, { useContext, useState, useEffect, useRef } from "react";
import "./Sidebar.css";
import { FaGear } from "react-icons/fa6";
import { FaSearch } from "react-icons/fa";
import axios from "axios";
import { ChatContext } from "../ContextAPI/ChatContext";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import { usePageManager } from "../ContextAPI/PageManagerContext";

export default function Sidebar() {
  const { searchQuery, setSearchQuery, setSearchResults } =
    useContext(ChatContext);
  const [showMenu, setShowMenu] = useState(false);
  const menuRef = useRef(null);
  const { goToPage } = usePageManager();
  const { goBack } = usePageManager();

  const navigate = useNavigate();

  const handleSearch = async () => {
    if (!searchQuery.trim()) return;

    try {
      const response = await axios.get(
        `http://localhost:8080/users/search?query=${searchQuery}`,
        { withCredentials: true }
      );
      setSearchResults(response.data);
    } catch (err) {
      console.error("Search failed", err);
      setSearchResults([]);
    }
  };

  const handleLogOut = async () => {
    try {
      const response = await axios.post(
        "http://localhost:8080/auth/logout",
        {},
        {
          withCredentials: true,
        }
      );
      if (response.status === 200) {
        toast.success("Logged out successfully!", { autoClose: 2000 });
        setSearchResults([]);
        setSearchQuery("");
        goBack();
        localStorage.removeItem("auth");
        localStorage.removeItem("user");
        document.cookie.split(";").forEach((cookie) => {
          const eqPos = cookie.indexOf("=");
          const name = eqPos > -1 ? cookie.substr(0, eqPos).trim() : cookie.trim();
          if (name === "userData") {
            document.cookie = `${name}=; Max-Age=0; path=/`;
            document.cookie = `${name}=; Max-Age=0; path=/main`;
            document.cookie = `${name}=; Max-Age=0; path=/chat`;
          }
        });
        setTimeout(() => {
          navigate("/login");
        }, 2000);
      }
    } catch (err) {
      toast.error("Logout Failed");
      console.error("Logout Failed", err);
      setSearchResults([]);
    }
  };

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setShowMenu(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);

    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  return (
    <div className="sidebar-header">
      <div className="settings" ref={menuRef}>
        <h2 className="logo">ChatAPP</h2>
        {/* 🌟 SETTINGS ICON + DROPDOWN */}
        <span className="settings-icon" onClick={() => setShowMenu(!showMenu)}>
          <FaGear />
        </span>

        {showMenu && (
          <div className="settings-menu">
            <button className="menu-item" onClick={() => goToPage("profile")}>Profile</button>
            <button className="menu-item" onClick={() => goToPage("request")}>Connection Requests</button>
            <button className="menu-item" onClick={handleLogOut}>
              Logout
            </button>
          </div>
        )}
      </div>
      <div className="search-box">
        <span className="search-icon">
          <FaSearch />
        </span>
        <input
          type="text"
          placeholder="Search people or start a new chat..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && handleSearch()}
        />
      </div>
    </div>
  );
}
