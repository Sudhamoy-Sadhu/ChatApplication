package com.example.chat.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.chat.Repository.ConnectionRequestRepo;

@Service
@EnableScheduling
public class ConnectionRequestCleanupService {

    @Autowired
    private ConnectionRequestRepo connectionRequestRepo;

    // Runs every day at 2 AM
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldRejectedRequests() {

        Instant cutoff = Instant.now().minus(7, ChronoUnit.DAYS);

        int deleted = connectionRequestRepo.deleteOldRejectedRequests(cutoff);

        if (deleted > 0) {
            System.out.println("Deleted " + deleted + " old rejected requests");
        }
    }
}
