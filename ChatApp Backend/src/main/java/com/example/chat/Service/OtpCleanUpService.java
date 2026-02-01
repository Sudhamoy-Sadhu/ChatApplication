package com.example.chat.Service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.chat.Repository.EmailOtpTokenRepo;

import jakarta.transaction.Transactional;

@Service
public class OtpCleanUpService {

    @Autowired
    private EmailOtpTokenRepo tokenRepo;

    @Scheduled(initialDelay = 60000, fixedRate = 600000)
    @Transactional
    public void cleanExpiredOtps() {
        tokenRepo.deleteByExpiryTimeBefore(LocalDateTime.now());
    }
}
