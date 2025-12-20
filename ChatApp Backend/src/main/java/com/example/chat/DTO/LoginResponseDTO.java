package com.example.chat.DTO;

import com.example.chat.Model.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDTO {
    private final Long id;
    private final String accessToken;
    private final String username;
    private final String email;
    private byte[] profilePicture;
    private final User.Status status;
}

