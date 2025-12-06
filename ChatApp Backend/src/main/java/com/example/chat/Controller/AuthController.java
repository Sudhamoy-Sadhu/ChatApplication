package com.example.chat.Controller;

import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
import com.example.chat.DTO.LoginResponseDTO;
import com.example.chat.Model.User;
import com.example.chat.Repository.SignUpRepo;
import com.example.chat.Service.JwtService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.Data;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "${cors.allowed-origins}")
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
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDTO req,
            HttpServletResponse response) {
        var userOpt = signUpRepo.findByEmail(req.getEmail());
        if (userOpt.isEmpty())
            return ResponseEntity.status(401).body("User Not Found!");

        User user = userOpt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        // Generate Tokens
        String access = jwtService.createAccessToken(user, Set.of("USER"));
        String refresh = jwtService.createRefreshToken(user);

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refresh)
                .httpOnly(true)
                .secure(true) // in production set true
                .path("/auth/refresh") // cookie sent only for refresh API
                .maxAge(7 * 24 * 3600)
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", refreshCookie.toString());

        ResponseCookie accessCookie = ResponseCookie.from("access_token", access)
                .httpOnly(true)
                .secure(true)
                .path("/") // sent for all backend APIs
                .maxAge(15 * 60) // 15 minutes
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());

        // Return ONLY ACCESS TOKEN
        return ResponseEntity.ok(new LoginResponseDTO(
                user.getId(),
                access,
                user.getUsername(),
                user.getEmail(),
                user.getStatus()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request,
            HttpServletResponse response) {

        String oldRefresh = null;
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (c.getName().equals("refresh_token")) {
                    oldRefresh = c.getValue();
                    break;
                }
            }
        }

        if (oldRefresh == null) {
            return ResponseEntity.status(401).body("Refresh token missing");
        }

        try {
            // Rotate refresh token
            String newRefresh = jwtService.rotateRefreshToken(oldRefresh);

            // Parse old token to get user id
            var jwt = com.nimbusds.jwt.SignedJWT.parse(oldRefresh);
            String subject = jwt.getJWTClaimsSet().getSubject();
            var user = signUpRepo.findById(Long.valueOf(subject)).orElseThrow();

            String newAccess = jwtService.createAccessToken(user, Set.of("USER"));

            ResponseCookie cookie = ResponseCookie.from("refresh_token", newRefresh)
                    .httpOnly(true)
                    .secure(true) // true in production
                    .path("/auth/refresh")
                    .maxAge(7 * 24 * 3600)
                    .sameSite("Strict")
                    .build();
            response.addHeader("Set-Cookie", cookie.toString());

            return ResponseEntity.ok(Map.of("accessToken", newAccess));
        } catch (Exception ex) {
            return ResponseEntity.status(401).body("Invalid refresh token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request,
            HttpServletResponse response) {

        String refresh = null;

        // Extract refresh_token from cookies
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (c.getName().equals("refresh_token")) {
                    refresh = c.getValue();
                    break;
                }
            }
        }

        // Revoke token in DB/Redis store
        if (refresh != null) {
            jwtService.revokeRefreshToken(refresh);
        }

        // 🔥 Clear REFRESH TOKEN COOKIE
        ResponseCookie clearRefresh = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true) // true in production
                .path("/auth/refresh") // same path as login
                .maxAge(0) // delete
                .sameSite("None") // must match login cookie
                .build();

        // 🔥 Clear ACCESS TOKEN COOKIE
        ResponseCookie clearAccess = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(true) // true in production
                .path("/") // same path as login
                .maxAge(0) // delete
                .sameSite("None") // must match login cookie
                .build();

        // Add both cookies to response
        response.addHeader("Set-Cookie", clearRefresh.toString());
        response.addHeader("Set-Cookie", clearAccess.toString());

        return ResponseEntity.ok(Map.of(
                "message", "Logged out successfully",
                "status", "success"));
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
