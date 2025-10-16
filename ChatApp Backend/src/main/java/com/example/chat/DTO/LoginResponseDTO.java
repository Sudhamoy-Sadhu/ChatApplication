package com.example.chat.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDTO {
    private final String accessToken;
    private final String refreshToken;
    private final String username;
    private final String email;
    private final String status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}

