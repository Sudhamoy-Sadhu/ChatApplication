import React, { useState, useContext } from "react";
import { FaEye, FaEyeSlash } from "react-icons/fa";
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import "./Login&SignUp.css";
import Carousel from "./Carousel";
import { AuthContext } from "../ContextAPI/AuthContext";

const LoginSignupPage = () => {
  const navigate = useNavigate();
  const { login } = useContext(AuthContext);
  const [isLogin, setIsLogin] = useState(true);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [formData, setFormData] = useState({
    email: "",
    password: "",
    confirmPassword: "",
    username: "",
  });

  const isFormValid = () => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (isLogin) {
      return (
        formData.email.trim() !== "" &&
        formData.password.trim() !== "" &&
        emailRegex.test(formData.email)
      );
    } else {
      return (
        formData.username.trim() !== "" &&
        formData.email.trim() !== "" &&
        formData.password.trim() !== "" &&
        formData.confirmPassword.trim() !== "" &&
        emailRegex.test(formData.email) &&
        formData.password === formData.confirmPassword
      );
    }
  };

  const validateForm = (formData, isLogin) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (isLogin) {
      if (!formData.email.trim() || !formData.password.trim()) {
        return { isValid: false, message: "All fields are required" };
      }
      if (!emailRegex.test(formData.email)) {
        return { isValid: false, message: "Invalid email format" };
      }
      return { isValid: true };
    } else {
      if (
        !formData.username.trim() ||
        !formData.email.trim() ||
        !formData.password.trim() ||
        !formData.confirmPassword.trim()
      ) {
        return { isValid: false, message: "All fields are required" };
      }
      if (!emailRegex.test(formData.email)) {
        return { isValid: false, message: "Invalid email format" };
      }
      if (formData.password !== formData.confirmPassword) {
        return { isValid: false, message: "Passwords do not match" };
      }
      return { isValid: true };
    }
  };

  const handleInputChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const validation = validateForm(formData, isLogin);

    if (!validation.isValid) {
      toast.error(`❌ ${validation.message}`, { autoClose: 2500 });
      return;
    }

    try {
      if (isLogin) {
        // 🔹 LOGIN API CALL
        const response = await axios.post("http://localhost:8080/auth/login", {
          email: formData.email,
          password: formData.password,
        });

        const { accessToken, username, email, status } = response.data;

        login(accessToken, { username, email, status });
        toast.success("✅ Login successful!", { autoClose: 1500 });
        setFormData({ email: "", password: "" });
        setTimeout(() => navigate("/"), 1800);
      } else {
        try {
          // 🔹 SIGNUP API CALL
          const response = await axios.post(
            "http://localhost:8080/signUp/register",
            {
              username: formData.username,
              email: formData.email,
              password: formData.password,
              confirmPassword: formData.confirmPassword,
            }
          );

          if (response.status === 200 || response.status === 201) {
            const successMessage =
              response.data.message || "Signup successful! Please login now.";
            toast.success(`🎉 ${successMessage}`, { autoClose: 2000 });
            setFormData({
              email: "",
              password: "",
              confirmPassword: "",
              username: "",
            });

            // Switch to login form after short delay
            setTimeout(() => setIsLogin(true), 2000);
          }
        } catch (error) {
          console.error("Signup error:", error);
          if (error.response) {
            // Show backend error message
            const errorMessage =
              error.response.data || "Signup failed. Please try again.";
            toast.error(`${errorMessage}`, { autoClose: 2500 });
          } else {
            toast.error("⚠️ Something went wrong during signup.", {
              autoClose: 2500,
            });
          }
        }
      }
    } catch (error) {
      console.error("Auth error:", error);

      if (error.response) {
        const errorMessage =
          error.response.data?.message ||
          error.response.data ||
          "Invalid credentials";
        toast.error(`${errorMessage}`, { autoClose: 2500 });
      } else if (error.request) {
        // No response (server might be down)
        toast.error("⚠️ Server not responding. Please try again later.", {
          autoClose: 2500,
        });
      } else {
        // Unexpected error
        toast.error("⚠️ Something went wrong.", { autoClose: 2500 });
      }
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter") {
      handleSubmit(e);
    }
  };

  const handleGoogleSignIn = () => {
    alert("Google sign in would be implemented here");
  };

  const handleFacebookSignIn = () => {
    alert("Facebook sign in would be implemented here");
  };

  return (
    <>
      <div className="login-signup-container" onKeyDown={handleKeyDown}>
        {/* Left Side - Carousel */}
        <div className="left-side">
          <Carousel />
        </div>
        {/* Right Side - Login/Signup Form */}
        <div className="right-side">
          <div className="form-side">
            <div className="form-container">
              <div className="form-card">
                <h1 className="form-title">{isLogin ? "Login" : "Sign Up"}</h1>

                <p className="form-subtitle">
                  {isLogin ? (
                    <>
                      New to chatting app?{" "}
                      <button
                        onClick={() => setIsLogin(false)}
                        className="toggle-link"
                      >
                        Signup Now
                      </button>
                    </>
                  ) : (
                    <>
                      Already have an account?{" "}
                      <button
                        onClick={() => setIsLogin(true)}
                        className="toggle-link"
                      >
                        Login Now
                      </button>
                    </>
                  )}
                </p>

                <div className="form-fields">
                  {/* Full Name (only for signup) */}
                  {!isLogin && (
                    <div className="input-group">
                      <input
                        type="text"
                        name="username"
                        placeholder="Username"
                        value={formData.username}
                        onChange={handleInputChange}
                        className="form-input"
                        required
                      />
                    </div>
                  )}

                  {/* Email */}
                  <div className="input-group">
                    <input
                      type="email"
                      name="email"
                      placeholder="Email Id"
                      value={formData.email}
                      onChange={handleInputChange}
                      className="form-input"
                      required
                    />
                  </div>

                  {/* Password */}
                  <div className="input-group">
                    <input
                      type={showPassword ? "text" : "password"}
                      name="password"
                      placeholder="Password"
                      value={formData.password}
                      onChange={handleInputChange}
                      className="form-input password-input"
                      required
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="password-toggle"
                    >
                      {showPassword ? (
                        <FaEyeSlash size={20} />
                      ) : (
                        <FaEye size={20} />
                      )}
                    </button>
                  </div>

                  {/* Confirm Password (only for signup) */}
                  {!isLogin && (
                    <div className="input-group">
                      <input
                        type={showConfirmPassword ? "text" : "password"}
                        name="confirmPassword"
                        placeholder="Confirm Password"
                        value={formData.confirmPassword}
                        onChange={handleInputChange}
                        className="form-input password-input"
                        required
                      />
                      <button
                        type="button"
                        onClick={() =>
                          setShowConfirmPassword(!showConfirmPassword)
                        }
                        className="password-toggle"
                      >
                        {showConfirmPassword ? (
                          <FaEyeSlash size={20} />
                        ) : (
                          <FaEye size={20} />
                        )}
                      </button>
                    </div>
                  )}

                  {/* Submit Button */}
                  <button
                    type="button"
                    onClick={handleSubmit}
                    className="submit-btn"
                    style={{
                      opacity: !isFormValid() ? 0.5 : 1,
                      cursor: !isFormValid() ? "not-allowed" : "pointer",
                    }}
                  >
                    {isLogin ? "Login" : "Sign Up"}
                  </button>
                  {isLogin && (
                    <div className="forgot-password-container">
                      <button
                        type="button"
                        className="forgot-password-link"
                        onClick={() => navigate("/forgot-password")}
                      >
                        Forgot Password?
                      </button>
                    </div>
                  )}
                </div>

                {/* Divider */}
                <div className="divider">
                  <div className="divider-line"></div>
                  <span className="divider-text">Or</span>
                  <div className="divider-line"></div>
                </div>

                {/* Social Login Buttons */}
                <div className="social-buttons">
                  <button
                    onClick={handleGoogleSignIn}
                    className="social-btn google-btn"
                  >
                    <svg className="social-icon" viewBox="0 0 24 24">
                      <path
                        fill="currentColor"
                        d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                      />
                      <path
                        fill="currentColor"
                        d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                      />
                      <path
                        fill="currentColor"
                        d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
                      />
                      <path
                        fill="currentColor"
                        d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
                      />
                    </svg>
                    <span>Sign {isLogin ? "in" : "up"} with Google</span>
                  </button>

                  <button
                    onClick={handleFacebookSignIn}
                    className="social-btn facebook-btn"
                  >
                    <svg
                      className="social-icon"
                      fill="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z" />
                    </svg>
                    <span>Sign {isLogin ? "in" : "up"} with Facebook</span>
                  </button>
                </div>

                {/* Terms and Conditions */}
                <p className="terms-text">
                  By signing {isLogin ? "in" : "up"}, I agree to the{" "}
                  <button className="terms-link">Terms and Conditions</button>
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
      <ToastContainer position="top-right" autoClose={2000} theme="light" />
    </>
  );
};

export default LoginSignupPage;
