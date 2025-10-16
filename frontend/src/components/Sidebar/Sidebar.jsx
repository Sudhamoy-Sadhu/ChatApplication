import React, { useContext } from "react";
import "./Sidebar.css";
import { FaGear } from "react-icons/fa6";
import { FaSearch } from "react-icons/fa";
import axios from "axios";
import { ChatContext } from "../ContextAPI/ChatContext";

export default function Sidebar() {
  const { searchQuery, setSearchQuery, setSearchResults } = useContext(ChatContext);

  const handleSearch = async () => {
    if (!searchQuery.trim()) return;

    try {
      const response = await axios.get(
        `http://localhost:8080/users/search?query=${searchQuery}`
      );
      setSearchResults(response.data); // update context with results
    } catch (err) {
      console.error("Search failed", err);
      setSearchResults([]);
    }
  };

  return (
    <div className="sidebar-header">
      <div className="settings">
        <h2 className="logo">ChatAPP</h2>
        <span><FaGear /></span>
      </div>
      <div className="search-box">
        <span className="search-icon"><FaSearch /></span>
        <input
          type="text"
          placeholder="Search people or start a new chat..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && handleSearch()} // search on Enter
        />
      </div>
    </div>
  );
}
