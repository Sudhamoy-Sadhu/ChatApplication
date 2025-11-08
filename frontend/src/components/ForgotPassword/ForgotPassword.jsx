import React, { useState } from "react";
import "./ForgotPassword.css";
import { toast } from "react-toastify";

export default function ForgotPassword() {
  const [formData, setFormData] = useState({
    email: "",
    otp: "",
    newPassword: "",
    confirmNewPass: "",
  });

  const isFormValid = (e) => {
    if (
      formData.email.trim() !== "" &&
      formData.otp.trim() !== "" &&
      formData.newPassword.trim() !== "" &&
      formData.confirmNewPass.trim() !== ""
    ) {
      return true;
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;

    // limit OTP to 6 digits
    if (name === "otp" && value.length > 6) return;

    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    if (!formData.email.trim()) {
      toast.error("Email is required");
      return;
    }
    if (!/^\d{6}$/.test(formData.otp)) {
      toast.error("OTP must be exactly 6 digits");
      return;
    }
    if (!formData.newPassword || !formData.confirmNewPass) {
      toast.error("Please fill out all password fields");
      return;
    }

    if (formData.newPassword !== formData.confirmNewPass) {
      toast.error("Passwords do not match");
      return;
    }
    toast.success("✅ Password changed successfully!");
  };
  return (
    <>
      <div className="Forgot-pass-main">
        <div className="forgot-form">
          <h2>Forgot Password</h2>
          <form className="forgot" onSubmit={handleSubmit}>
            <div className="email-otp">
              <input
                type="email"
                name="email"
                placeholder="Enter Your Email"
                value={formData.email}
                onChange={handleChange}
                required
              />
              <button
                className="get-otp"
                onClick={() => toast.info("📨 OTP sent to your email")}
              >
                Get OTP
              </button>
            </div>
            <input
              type="number"
              name="otp"
              placeholder="Enter OTP"
              value={formData.otp}
              onChange={handleChange}
              required
            />
            <input
              type="password"
              name="newPassword"
              placeholder="Enter new password"
              value={formData.newPassword}
              onChange={handleChange}
              required
            />
            <input
              type="password"
              name="confirmNewPass"
              placeholder="Confirm new password"
              value={formData.confirmNewPass}
              onChange={handleChange}
              required
            />
            <button className="change-pass" disabled={!isFormValid()}>Change Password</button>
          </form>
        </div>
      </div>
    </>
  );
}
