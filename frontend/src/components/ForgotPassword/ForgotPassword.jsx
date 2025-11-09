import React, { useState } from "react";
import "./ForgotPassword.css";
import { toast } from "react-toastify";
import axios from "axios";
import { useNavigate } from 'react-router-dom';

export default function ForgotPassword() {
  const [formData, setFormData] = useState({
    email: "",
    otp: "",
    newPassword: "",
    confirmNewPass: "",
  });
  const [otpSent, setOtpSent] = useState(false);

  const navigate = useNavigate();

  const isFormValid = (e) => {
    if (
      formData.email.trim() !== "" &&
      formData.otp.trim() !== "" &&
      formData.newPassword.trim() !== "" &&
      formData.confirmNewPass.trim() !== ""
    ) {
      return true;
    } else return false;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;

    // limit OTP to 6 digits
    if (name === "otp" && value.length > 6) return;

    setFormData({ ...formData, [name]: value });
  };

  const handleSendOtp = async (e) => {
    e.preventDefault();

    if (!formData.email.trim()) {
      toast.error("Email is required");
      return;
    }

    try {
      const response = await axios.post(
        "http://localhost:8080/forgot-password/get-otp",
        { email: formData.email }
      );
      toast.success("📨 OTP sent successfully to your email!");
      console.log("OTP Response:", response.data);
      setOtpSent(true);
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to send OTP");
      console.error("Error sending OTP:", error);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

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
    try {
      const response = await axios.post(
        "http://localhost:8080/forgot-password/change-password",
        {
          email: formData.email,
          otp: formData.otp,
          newPassword: formData.newPassword,
          confirmNewPass: formData.confirmNewPass,
        }
      );
      toast.success("Password Changed Successfully!");
      console.log("Change Password Response:", response.data);
      setOtpSent(false);
      setFormData({ email: "", otp: "", newPassword: "", confirmNewPass: "" });
      navigate("/");
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to change password");
      console.error("Error changing password:", error);
    }
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
                onClick={handleSendOtp}
              >
                Get OTP
              </button>
            </div>

            {otpSent && (
              <>
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
                <button className="change-pass" disabled={!isFormValid()}>
                  Change Password
                </button>
              </>
            )}
          </form>
        </div>
      </div>
    </>
  );
}
