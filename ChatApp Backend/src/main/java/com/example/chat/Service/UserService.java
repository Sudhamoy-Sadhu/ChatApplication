package com.example.chat.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.chat.DTO.UserSearchDTO;
import com.example.chat.Model.User;
import com.example.chat.Repository.UserRepo;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    public List<UserSearchDTO> searchUsers(String query) {
        List<User> users = userRepo.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);

        return users.stream()
                .map(user -> new UserSearchDTO(
                user.getUsername(),
                user.getEmail(),
                user.getProfilePicture()
        ))
                .collect(Collectors.toList());
    }
    

    public void updateProfilePicture(Long userId, MultipartFile file) throws IOException {
        // 1. Check file size
        if (file.getSize() > 10 * 1024 * 1024) { // 10 MB
            throw new IllegalArgumentException("File size exceeds 10MB");
        }

        // 2. Check file type
        String contentType = file.getContentType();
        if (!contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Invalid file format");
        }

        // 3. Fetch user
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 4. Save image as byte array
        user.setProfilePicture(file.getBytes());
        userRepo.save(user);
    }

}
