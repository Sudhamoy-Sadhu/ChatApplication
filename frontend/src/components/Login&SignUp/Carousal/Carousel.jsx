import React, { useState, useEffect } from "react";
import "./Carousel.css";


function Carousel(){
  
const HashIcon = ({ size = 16 }) => (
    <svg
        xmlns="http://www.w3.org/2000/svg"
        width={size}
        height={size}
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
        strokeWidth={2}
    >
        <path d="M4 9h16M4 15h16M10 3L8 21M16 3l-2 18" />
    </svg>
);

const HeartIcon = ({ size = 16 }) => (
    <svg
        xmlns="http://www.w3.org/2000/svg"
        width={size}
        height={size}
        fill="currentColor"
        viewBox="0 0 24 24"
    >
        <path d="M12 21s-6.716-4.617-9.878-8.485C-1.04 9.644 2.582 3 8.364 3 10.67 3 12 5.194 12 5.194S13.33 3 15.636 3c5.782 0 9.404 6.644 6.242 9.515C18.716 16.383 12 21 12 21z" />
    </svg>
);

  const slides = [
    // Slide 1 (your existing code here)
    (
      <div className="illustration-side">
        <div className="illustration-content">
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
              <HeartIcon />
            </div>
            <div className="floating-icon hash-blue">
              <HashIcon />
            </div>
            <div className="floating-icon hash-purple">
              <HashIcon />
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
            Fast chats, real connections — connect, share, smile.
          </h2>
        </div>
      </div>
    ),

    // Slide 2 (you can add another image/illustration)
    (
      <div className="illustration-side">
        <div className="illustration-content">
          <img src="../assets/77881.jpg" alt="Slide 2" class/>
        </div>
      </div>
    ),

    // Slide 3
    (
      <div className="illustration-side">
        <div className="illustration-content">
          <img src="/images/slide3.png" alt="Slide 3" />
        </div>
      </div>
    ),
  ];

  const [currentIndex, setCurrentIndex] = useState(0);

  // Auto-slide every 2s
  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentIndex((prev) => (prev + 1) % slides.length);
    }, 2000);
    return () => clearInterval(interval);
  }, [slides.length]);

  return (
    <div className="carousel">
      <div
        className="carousel-inner"
        style={{ transform: `translateX(-${currentIndex * 100}%)` }}
      >
        {slides.map((slide, index) => (
          <div className="carousel-item" key={index}>
            {slide}
          </div>
        ))}
      </div>

      {/* Dots indicator */}
      <div className="dots-indicator">
        {slides.map((_, index) => (
          <div
            key={index}
            className={`dot ${index === currentIndex ? "active" : "inactive"}`}
            onClick={() => setCurrentIndex(index)}
          ></div>
        ))}
      </div>
    </div>
  );
};

export default Carousel;
