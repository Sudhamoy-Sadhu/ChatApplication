package com.example.chat.DTO;

import com.example.chat.Model.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private byte[] profilePicture;
    private User.Status status;
}
