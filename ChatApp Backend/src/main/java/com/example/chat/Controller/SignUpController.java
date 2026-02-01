package com.example.chat.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chat.DTO.ForgotPassDTO;
import com.example.chat.DTO.ForgotPassOtpDTO;
import com.example.chat.DTO.SignUpDTO;
import com.example.chat.DTO.VerifyOtpDTO;
import com.example.chat.Model.EmailOtpToken.OtpPurpose;
import com.example.chat.Service.JwtService;
import com.example.chat.Service.OtpService;
import com.example.chat.Service.SignUpService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/signUp")
@CrossOrigin(origins = "${cors.allowed-origins}")
public class SignUpController {

    @Autowired
    private SignUpService signUpService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private JwtService jwtService;

    // STEP 1: Send OTP
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody @Valid ForgotPassOtpDTO dto) {
        try {
            otpService.sendOtp(dto.getEmail(), OtpPurpose.SIGNUP);
            return ResponseEntity.ok("OTP sent to email");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // STEP 2: Verify OTP and issue verification JWT
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody @Valid VerifyOtpDTO dto) {
        try {
            otpService.verifyOtp(dto.getEmail(), dto.getOtp(), OtpPurpose.SIGNUP);

            String token = jwtService.createEmailVerificationToken(dto.getEmail());

            return ResponseEntity.ok(token);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SignUpDTO dto) {

        System.out.println("JWT -> " + jwt);

        String emailFromToken = jwt.getSubject();

        if (!emailFromToken.equals(dto.getEmail())) {
            throw new RuntimeException("Email mismatch");
        }

        try {
            signUpService.registerUser(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationErrors(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getAllErrors()
                .get(0)
                .getDefaultMessage();
        return ResponseEntity.badRequest().body(errorMessage);
    }
}
