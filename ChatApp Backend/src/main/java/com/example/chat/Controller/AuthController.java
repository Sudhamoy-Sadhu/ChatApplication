package com.example.chat.Controller;

import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chat.DTO.LoginRequestDTO;
import com.example.chat.Model.User;
import com.example.chat.Repository.SignUpRepo;
import com.example.chat.Service.JwtService;

import jakarta.validation.Valid;
import lombok.Data;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final SignUpRepo signUpRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(SignUpRepo signUpRepo, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.signUpRepo = signUpRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDTO req) {
        var userOpt = signUpRepo.findByEmail(req.getEmail());
        if (userOpt.isEmpty())
            return ResponseEntity.status(401).body("Invalid credentials");

        User user = userOpt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        return ResponseEntity.ok(jwtService.loginUser(user));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest req) {
        try {
            String newRefresh = jwtService.rotateRefreshToken(req.getRefreshToken());
            // find the subject from the newRefresh? Simpler: parse old token to get user id
            // and create new access for that user
            // but we can deduce user from DB lookup before rotation if needed. We'll parse
            // old token:
            var jwt = com.nimbusds.jwt.SignedJWT.parse(req.getRefreshToken());
            String subject = jwt.getJWTClaimsSet().getSubject();
            // You must load user by id
            var user = signUpRepo.findById(Long.valueOf(subject)).orElseThrow();
            String newAccess = jwtService.createAccessToken(user, Set.of("USER"));
            return ResponseEntity.ok(new LoginResponse(newAccess, newRefresh));
        } catch (Exception ex) {
            return ResponseEntity.status(401).body("Invalid refresh token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshRequest req) {
        jwtService.revokeRefreshToken(req.getRefreshToken());
        return ResponseEntity.ok("Logged out");
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remove "Bearer "
            String username = jwtService.extractUsername(token);

            if (username != null && jwtService.validateAccessToken(token)) {
                return ResponseEntity.ok().body(Map.of("valid", true));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
    }

    @Data
    static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    static class RefreshRequest {
        private String refreshToken;

        private String getRefreshToken() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Data
    static class LoginResponse {
        private final String accessToken;
        private final String refreshToken;
    }
}
