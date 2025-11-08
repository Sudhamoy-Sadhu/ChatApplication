package com.example.chat.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.chat.Model.User;
import com.example.chat.Repository.UserRepo;

import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepo userRepo;

    public String sendOtpEmail(String toEmail) {

        Optional<User> existingUser = userRepo.findByEmail(toEmail);
        if (existingUser.isEmpty()) {
            throw new RuntimeException("No user found with email: " + toEmail);
        }
        String otp = generateOtp();

        String subject = "Your OTP Code";
        String body = "Your OTP for password reset is: " + otp + "\n\n" +
                      "This OTP is valid for 5 minutes.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("your_email@gmail.com");

        mailSender.send(message);

        System.out.println("✅ OTP sent to: " + toEmail);
        return otp;
    }

    private String generateOtp() {
        Random random = new Random();
        int otpValue = 100000 + random.nextInt(900000);
        return String.valueOf(otpValue);
    }    
}
