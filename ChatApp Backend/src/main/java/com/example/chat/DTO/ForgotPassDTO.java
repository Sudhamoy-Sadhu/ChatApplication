package com.example.chat.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ForgotPassDTO {

    @NotBlank(message = "Email is required")
    private String email;
    
    @NotBlank
    @Pattern(regexp = "\\d{6}", message = "OTP must be exactly 6 digits")
    private String otp;

    @jakarta.validation.constraints.Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$", message = "Password must be at least 8 characters long and include an uppercase letter, a lowercase letter, a number, and a special character")
    private String newPassword;

    @NotBlank(message = "Confirm Password should not be empty")
    private String confirmNewPass;
}
