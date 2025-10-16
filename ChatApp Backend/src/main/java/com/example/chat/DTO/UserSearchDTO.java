package com.example.chat.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserSearchDTO {
    private String username;
    private String email;
    private byte[] profilePicture;
}