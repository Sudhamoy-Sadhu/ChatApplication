package com.example.chat.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDTO {
    private final String accessToken;
    private final String refreshToken;
}

