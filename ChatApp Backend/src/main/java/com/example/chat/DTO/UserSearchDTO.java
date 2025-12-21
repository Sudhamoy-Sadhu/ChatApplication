package com.example.chat.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserSearchDTO {
    private Long id;
    private String username;
    private String email;
    private String profilePicture;
    private boolean exists; 
    private boolean connected;
}