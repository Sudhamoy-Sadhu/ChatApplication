import React, { useContext, useState } from "react";
import "./Profile.css"
import { AuthContext } from "../ContextAPI/AuthContext";

export default function Profile() {
    const [bio, setBio] = useState("");
    const [theme, setTheme] = useState("light");
    const [profilePicPrivacy, setProfilePicPrivacy] = useState("everyone");
    const [lastSeenPrivacy, setLastSeenPrivacy] = useState("everyone");
    const {user} = useContext(AuthContext);

    return (
        <>
            <div className="profile-main">

                {/* Profile Picture Section */}
                <div className="profile-img-div">
                    <img src={user.profileImageUrl || "/assets/default-logo.png"} alt="DP" />
                    <button>Edit</button>
                </div>

                {/* Basic Info */}
                <div className="profile-name">
                    <h1>{user.username}</h1>
                    <p className="profile-email">{user.email}</p>
                </div>

                {/* About / Bio / Status */}
                <div className="profile-section">
                    <label>About / Status</label>
                    <textarea
                        maxLength={150}
                        value={bio}
                        onChange={(e) => setBio(e.target.value)}
                        placeholder="Busy, Available, Open to chat..."
                    />
                    <small>{bio.length}/150</small>
                </div>

                {/* Theme Preference */}
                <div className="profile-section">
                    <label>Chat Theme</label>
                    <select
                        value={theme}
                        onChange={(e) => setTheme(e.target.value)}
                    >
                        <option value="light">Light</option>
                        <option value="dark">Dark</option>
                    </select>
                </div>

                {/* Privacy Controls */}
                <div className="profile-section">
                    <label>Who can see my profile picture?</label>
                    <select
                        value={profilePicPrivacy}
                        onChange={(e) => setProfilePicPrivacy(e.target.value)}
                    >
                        <option value="everyone">Everyone</option>
                        <option value="contacts">My Contacts</option>
                        <option value="nobody">Nobody</option>
                    </select>
                </div>

                <div className="profile-section">
                    <label>Who can see my last seen?</label>
                    <select
                        value={lastSeenPrivacy}
                        onChange={(e) => setLastSeenPrivacy(e.target.value)}
                    >
                        <option value="everyone">Everyone</option>
                        <option value="contacts">My Contacts</option>
                        <option value="nobody">Nobody</option>
                    </select>
                </div>

            </div>
        </>
    );
}
