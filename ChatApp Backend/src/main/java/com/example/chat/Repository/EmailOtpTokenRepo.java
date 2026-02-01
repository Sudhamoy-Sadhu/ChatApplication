package com.example.chat.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.chat.Model.EmailOtpToken;
import com.example.chat.Model.EmailOtpToken.OtpPurpose;

@Repository
public interface EmailOtpTokenRepo extends JpaRepository<EmailOtpToken, Long> {
    Optional<EmailOtpToken> findByEmailAndPurpose(String email, OtpPurpose purpose);

    void deleteByEmailAndPurpose(String email, OtpPurpose purpose);

    void deleteByExpiryTimeBefore(LocalDateTime time);

}
