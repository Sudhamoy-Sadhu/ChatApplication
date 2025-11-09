package com.example.chat.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.chat.Model.PasswordResetToken;
import com.example.chat.Model.User;
import com.example.chat.Repository.PasswordResetTokenRepo;
import com.example.chat.Repository.UserRepo;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordResetTokenRepo tokenRepo;

    public String sendOtpEmail(String toEmail) {
        Optional<User> existingUser = userRepo.findByEmail(toEmail);
        if (existingUser.isEmpty()) {
            throw new RuntimeException("No user found with email: " + toEmail);
        }

        String otp = generateOtp();

        // Set expiry time (5 minutes from now)
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);

        // Remove old OTPs for this email (if any)
        tokenRepo.deleteByEmail(toEmail);

        // Save new OTP
        PasswordResetToken token = new PasswordResetToken(toEmail, otp, expiryTime);
        tokenRepo.save(token);

        // Send mail
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP for password reset is: " + otp + "\n\nThis OTP is valid for 5 minutes.");
        message.setFrom("chatapp2400@gmail.com");

        mailSender.send(message);

        System.out.println("✅ OTP sent to: " + toEmail);
        return otp;
    }

    public boolean verifyOtp(String email, String enteredOtp) {
        PasswordResetToken token = tokenRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No OTP found for this email"));

        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        return token.getOtp().equals(enteredOtp);
    }

    private String generateOtp() {
        Random random = new Random();
        int otpValue = 100000 + random.nextInt(900000);
        return String.valueOf(otpValue);
    }
}
