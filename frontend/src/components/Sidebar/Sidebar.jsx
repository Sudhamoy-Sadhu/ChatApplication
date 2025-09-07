import React from "react";
import "./Sidebar.css";
import { FaGear } from "react-icons/fa6";
import { FaSearch } from "react-icons/fa";

export default function Sidebar() {
    return (
        <div className="sidebar-header">
            <div className="settings">
                <h2 className="logo">ChatAPP</h2>
               <span><FaGear /></span>
            </div>
            <div className="search-box">
                <span><FaSearch/></span>
                <input type="text" placeholder="Search people or start a new chat..." />
            </div>
        </div>
    );
}
