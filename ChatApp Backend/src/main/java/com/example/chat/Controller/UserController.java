package com.example.chat.Controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.chat.DTO.UserResponseDTO;
import com.example.chat.DTO.UserSearchDTO;
import com.example.chat.Repository.UserRepo;
import com.example.chat.Service.UserService;

@RestController
@CrossOrigin(origins = "${cors.allowed-origins}")
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchDTO>> searchUsers(
            @RequestParam String query,
            Authentication authentication) {

        try {
            Long loggedInUserId = Long.valueOf(authentication.getName());
            List<UserSearchDTO> users = userService.searchUsers(query, loggedInUserId);
            return ResponseEntity.ok(users);
        } catch (NumberFormatException nfe) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/profile-picture")
    public ResponseEntity<byte[]> uploadProfilePicture(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        try {
            Long userId = Long.valueOf(authentication.getName());
            byte[] compressedImage = userService.updateProfilePicture(userId, file);

            // Return the compressed bytes immediately so the frontend can update
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(compressedImage);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/profile-data")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        try{
            Long id = Long.valueOf(authentication.getName());
            UserResponseDTO user = userService.getProfileData(id);
            return ResponseEntity.ok(user);
        }catch (NullPointerException e){
            return ResponseEntity.badRequest().body("Unable to fetch Profile data");
        }catch (Exception e){
            return ResponseEntity.internalServerError().body("Server error, Please try again later");
        }
    }
}
