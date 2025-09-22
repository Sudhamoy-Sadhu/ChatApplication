// LoginSignupPage.jsx
import React, { useState, useEffect } from 'react';
import { FaEye, FaEyeSlash, FaHeart, FaHashtag, FaChevronLeft, FaChevronRight, FaComments, FaUsers, FaBolt } from 'react-icons/fa';
import './Login&SignUp.css';

const LoginSignupPage = () => {
    const [isLogin, setIsLogin] = useState(true);
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [currentSlide, setCurrentSlide] = useState(0);
    const [formData, setFormData] = useState({
        email: '',
        password: '',
        confirmPassword: '',
        fullName: ''
    });

    const slides = [
        {
            title: "Fast chats, real connections — connect, share, smile.",
            icon: FaComments,
            theme: "slide-theme-1"
        },
        {
            title: "Join millions of users worldwide — your community awaits.",
            icon: FaUsers,
            theme: "slide-theme-2"
        },
        {
            title: "Lightning fast messaging — experience the speed of connection.",
            icon: FaBolt,
            theme: "slide-theme-3"
        }
    ];

    // Auto-slide functionality
    useEffect(() => {
        const interval = setInterval(() => {
            setCurrentSlide((prev) => (prev + 1) % slides.length);
        }, 4000);
        return () => clearInterval(interval);
    }, [slides.length]);

    const nextSlide = () => {
        setCurrentSlide((prev) => (prev + 1) % slides.length);
    };

    const prevSlide = () => {
        setCurrentSlide((prev) => (prev - 1 + slides.length) % slides.length);
    };

    const goToSlide = (index) => {
        setCurrentSlide(index);
    };

    const handleInputChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (isLogin) {
            console.log('Login attempt:', { email: formData.email, password: formData.password });
            alert('Login functionality would be implemented here');
        } else {
            console.log('Signup attempt:', formData);
            alert('Signup functionality would be implemented here');
        }
    };

    const handleGoogleSignIn = () => {
        alert('Google sign in would be implemented here');
    };

    const handleFacebookSignIn = () => {
        alert('Facebook sign in would be implemented here');
    };

    return (
        <div className="login-signup-container">
            {/* Left Side - Carousel */}
            <div className="illustration-side">
                <div className="carousel-container">
                    {/* Carousel Navigation */}
                    <button className="carousel-nav prev" onClick={prevSlide}>
                        <FaChevronLeft />
                    </button>
                    <button className="carousel-nav next" onClick={nextSlide}>
                        <FaChevronRight />
                    </button>

                    <div className="carousel-wrapper">
                        <div
                            className="carousel-slides"
                            style={{ transform: `translateX(-${currentSlide * 100}%)` }}
                        >
                            {slides.map((slide, index) => (
                                <div key={index} className={`carousel-slide ${slide.theme}`}>
                                    {/* Chat Illustration */}
                                    <div className="chat-illustration">
                                        <div className="chat-container">
                                            {/* Main chat bubble */}
                                            <div className="main-chat-bubble">
                                                <div className="chat-line line-3-4"></div>
                                                <div className="chat-line line-1-2"></div>
                                                <div className="chat-tail"></div>
                                            </div>

                                            {/* Secondary chat bubble */}
                                            <div className="secondary-chat-bubble">
                                                <div className="chat-line line-2-3"></div>
                                                <div className="chat-line line-3-4"></div>
                                                <div className="secondary-chat-tail"></div>
                                            </div>
                                        </div>

                                        {/* Floating icons */}
                                        <div className="floating-icon heart">
                                            <FaHeart className="social-icon" />
                                        </div>
                                        <div className="floating-icon hash-blue">
                                            <slide.icon className="social-icon" />
                                        </div>
                                        <div className="floating-icon hash-purple">
                                            <FaHashtag className="social-icon" />
                                        </div>

                                        {/* Characters */}
                                        <div className="character-avatar character-large">
                                            <div className="character-inner"></div>
                                        </div>
                                        <div className="character-avatar character-small">
                                            <div className="character-inner"></div>
                                        </div>
                                    </div>

                                    <h2 className="main-title">
                                        {slide.title}
                                    </h2>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Dots indicator */}
                    <div className="dots-indicator">
                        {slides.map((_, index) => (
                            <div
                                key={index}
                                className={`dot ${index === currentSlide ? 'active' : 'inactive'}`}
                                onClick={() => goToSlide(index)}
                            ></div>
                        ))}
                    </div>
                </div>
            </div>

            {/* Right Side - Login/Signup Form */}
            <div className="form-side">
                <div className="form-container">
                    <div className="form-card">
                        <h1 className="form-title">
                            {isLogin ? 'Login' : 'Sign Up'}
                        </h1>

                        <p className="form-subtitle">
                            {isLogin ? (
                                <>
                                    New to chatting app?{' '}
                                    <button
                                        onClick={() => setIsLogin(false)}
                                        className="toggle-link"
                                    >
                                        Signup Now
                                    </button>
                                </>
                            ) : (
                                <>
                                    Already have an account?{' '}
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
                                        name="fullName"
                                        placeholder="Full Name"
                                        value={formData.fullName}
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
                                    type={showPassword ? 'text' : 'password'}
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
                                    {showPassword ? <FaEyeSlash size={20} /> : <FaEye size={20} />}
                                </button>
                            </div>

                            {/* Confirm Password (only for signup) */}
                            {!isLogin && (
                                <div className="input-group">
                                    <input
                                        type={showConfirmPassword ? 'text' : 'password'}
                                        name="confirmPassword"
                                        placeholder="Confirm Password"
                                        value={formData.confirmPassword}
                                        onChange={handleInputChange}
                                        className="form-input password-input"
                                        required
                                    />
                                    <button
                                        type="button"
                                        onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                        className="password-toggle"
                                    >
                                        {showConfirmPassword ? <FaEyeSlash size={20} /> : <FaEye size={20} />}
                                    </button>
                                </div>
                            )}

                            {/* Submit Button */}
                            <button
                                type="button"
                                onClick={handleSubmit}
                                className="submit-btn"
                            >
                                {isLogin ? 'Login' : 'Sign Up'}
                            </button>
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
                                    <path fill="currentColor" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                                    <path fill="currentColor" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                                    <path fill="currentColor" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" />
                                    <path fill="currentColor" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
                                </svg>
                                <span>Sign {isLogin ? 'in' : 'up'} with Google</span>
                            </button>

                            <button
                                onClick={handleFacebookSignIn}
                                className="social-btn facebook-btn"
                            >
                                <svg className="social-icon" fill="currentColor" viewBox="0 0 24 24">
                                    <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z" />
                                </svg>
                                <span>Sign {isLogin ? 'in' : 'up'} with Facebook</span>
                            </button>
                        </div>

                        {/* Terms and Conditions */}
                        <p className="terms-text">
                            By signing {isLogin ? 'in' : 'up'}, I agree to the{' '}
                            <button className="terms-link">
                                Terms and Conditions
                            </button>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LoginSignupPage;
