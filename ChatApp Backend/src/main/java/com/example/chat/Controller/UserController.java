package com.example.chat.Controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.chat.DTO.UserSearchDTO;
import com.example.chat.Service.UserService;

@RestController
@CrossOrigin(origins = "${cors.allowed-origins}")
@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private UserService userService;

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchDTO>> searchUsers(@RequestParam("query") String query) {
        try {
            List<UserSearchDTO> users = userService.searchByUsernameOrEmail(query);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // -----------------------------
    // API 2: Update profile picture
    // -----------------------------
    @PostMapping("/{userId}/profile-picture")
    public ResponseEntity<String> updateProfilePicture(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {

        try {
            userService.updateProfilePicture(userId, file);
            return ResponseEntity.ok("Profile picture updated successfully");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error saving profile picture");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
