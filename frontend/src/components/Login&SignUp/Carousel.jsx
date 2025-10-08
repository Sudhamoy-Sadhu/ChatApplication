import React from "react";
import { Swiper, SwiperSlide } from "swiper/react";
import { Pagination, Autoplay } from "swiper/modules";

import "swiper/css";
import "swiper/css/pagination";
import "./Carousel.css";

const slides = [
  {
    image: "/assets/chatting1.jpg",
    text: "Fast chats, real connections — connect, share, smile.",
  },
  {
    image: "/assets/chatting2.jpg",
    text: "Join millions of users worldwide — your community awaits.",
  },
  {
    image: "/assets/chatting3.jpg",
    text: "Lightning fast messaging — experience the speed of connection.",
  },
];

const Carousel = () => {
  return (
    <div className="carousel-container">
      <Swiper
        modules={[Pagination, Autoplay]}
        pagination={{ clickable: true }}
        autoplay={{ delay: 3000, disableOnInteraction: false }}
        loop={true}
      >
        {slides.map((slide, index) => (
          <SwiperSlide key={index}>
            <div className="slide-content">
              <img src={slide.image} alt={`slide-${index}`} className="slide-image" />
              <h2 className="slide-text">{slide.text}</h2>
            </div>
          </SwiperSlide>
        ))}
      </Swiper>
    </div>
  );
};

export default Carousel;
