package com.example.chat.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvitationDTO {

    @NotBlank
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank
    private String name;
}
