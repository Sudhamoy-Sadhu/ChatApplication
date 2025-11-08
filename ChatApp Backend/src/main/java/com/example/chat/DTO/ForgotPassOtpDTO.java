package com.example.chat.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ForgotPassOtpDTO {
    @NotBlank(message = "Email should not be empty")
    @Email(message = "Invalid email format")
    private String email;
}
