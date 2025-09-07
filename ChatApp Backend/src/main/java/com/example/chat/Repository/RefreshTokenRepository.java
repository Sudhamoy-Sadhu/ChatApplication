package com.example.chat.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chat.Model.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // Find a refresh token by its token ID and check if it's active
    Optional<RefreshToken> findByTokenIdAndActiveTrue(String tokenId);

    // Optionally, find by token string if you store the serialized token
    Optional<RefreshToken> findByToken(String token);
}
