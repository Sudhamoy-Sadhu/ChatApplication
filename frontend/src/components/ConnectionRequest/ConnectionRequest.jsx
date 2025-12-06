import React, { useEffect, useState, useContext } from "react";
import axios from "axios";
import "./ConnectionRequest.css";
import { AuthContext } from "../ContextAPI/AuthContext";

export default function ConnectionRequests() {
    const { user } = useContext(AuthContext);
    const [requests, setRequests] = useState([]);

    // Fetch connection requests
    useEffect(() => {
        const fetchRequests = async () => {
            try {
                const res = await axios.get(
                    "http://localhost:8080/connection/getAllConnections",
                    { withCredentials: true }
                );
                setRequests(res.data);
            } catch (err) {
                console.log("Error fetching requests:", err);
            }
        };

        fetchRequests();
    }, []);

    // Handle Request Actions
    const handleAction = async (requestId, action) => {
        try {
            await axios.put(
                `http://localhost:8080/connection/${action}/${requestId}`,
                {},
                { withCredentials: true }
            );

            // Update UI instantly
            setRequests((prev) => prev.filter((req) => req.requestId !== requestId));
        } catch (error) {
            console.log("Action error →", error);
            alert(error.response?.data || "Something went wrong");
        }
    };

    return (
        <div className="cr-wrapper">
            <h2 className="cr-title">Connection Requests</h2>

            <div className="cr-list">
                {requests.length === 0 ? (
                    <p className="cr-empty">No connection requests.</p>
                ) : (
                    requests.map((req) => {
                        const loggedInUserId = user?.id;
                        const isRequester = req.requester.id === loggedInUserId;
                        const isTarget = req.target.id === loggedInUserId;

                        // Display info of the other user
                        const displayUser = isRequester ? req.target : req.requester;

                        return (
                            <div key={req.requestId} className="cr-card">
                                 {/* Request Date */}
                                <div className="cr-date">
                                    Requested on
                                    <p>{" "}</p>
                                    {new Date(req.createdAt).toLocaleDateString("en-IN", {
                                        day: "2-digit",
                                        month: "short",
                                        year: "numeric",
                                    })}
                                </div>
                                {/* Profile Pic */}
                                <div className="cr-avatar-section">
                                    <img
                                        src={displayUser.profilePic || "/assets/default-logo.png"}
                                        alt="profile"
                                        className="cr-avatar"
                                    />
                                </div>

                                {/* User Info */}
                                <div className="cr-info">
                                    <p className="cr-name">{displayUser.name}</p>
                                    <p className="cr-email">{displayUser.email}</p>
                                </div>


                                {/* Action Buttons */}
                                <div className="cr-action">
                                    {req.status === "PENDING" && isRequester && (
                                        <button
                                            className="cr-btn cr-cancel"
                                            onClick={() => handleAction(req.requestId, "cancel")}
                                        >
                                            Cancel Request
                                        </button>
                                    )}

                                    {req.status === "PENDING" && isTarget && (
                                        <>
                                            <button
                                                className="cr-btn cr-accept"
                                                onClick={() => handleAction(req.requestId, "accept")}
                                            >
                                                Accept
                                            </button>
                                            <button
                                                className="cr-btn cr-reject"
                                                onClick={() => handleAction(req.requestId, "reject")}
                                            >
                                                Reject
                                            </button>
                                        </>
                                    )}

                                    {req.status === "ACCEPTED" && (
                                        <p className="cr-connected">Connected ✔</p>
                                    )}

                                    {req.status === "REJECTED" && isTarget && (
                                        <p className="cr-rejected">Rejected ❌</p>
                                    )}
                                </div>
                            </div>
                        );
                    })
                )}
            </div>
        </div>
    );
}
