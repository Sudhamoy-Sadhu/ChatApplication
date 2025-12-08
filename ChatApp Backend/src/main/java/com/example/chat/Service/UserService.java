package com.example.chat.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.chat.DTO.ForgotPassDTO;
import com.example.chat.DTO.UserSearchDTO;
import com.example.chat.Model.User;
import com.example.chat.Repository.ContactRepo;
import com.example.chat.Repository.UserRepo;
import jakarta.transaction.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ContactRepo contactRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OtpService otpService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public List<UserSearchDTO> searchUsers(String query, Long loggedInUserId) {

        List<User> users = userRepo.searchUsersExcludeLoggedIn(query, loggedInUserId);

        if (users.isEmpty()) {
            return List.of(new UserSearchDTO(
                    null,
                    null,
                    query,
                    null,
                    false,
                    false));
        }

        return users.stream()
                .map(user -> {
                    boolean connected = contactRepo.existsConnection(loggedInUserId, user.getId());
                    return new UserSearchDTO(
                            user.getId(),
                            user.getUsername(),
                            user.getEmail(),
                            user.getProfilePicture(),
                            true,
                            connected);
                })
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

    public void changePassword(ForgotPassDTO forgotPassDTO) {

        String email = forgotPassDTO.getEmail().trim();

        if (!otpService.verifyOtp(email, forgotPassDTO.getOtp())) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        if (!forgotPassDTO.getNewPassword().equals(forgotPassDTO.getConfirmNewPass())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPassword(passwordEncoder.encode(forgotPassDTO.getNewPassword()));
        userRepo.save(user);
    }

    @Transactional
    public void setStatusOnline(Long id) {
        User user = userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(User.Status.ACTIVE);
        userRepo.save(user);
        broadcastStatusChange(user);
    }

    @Transactional
    public void setStatusOffline(Long id) {
        User user = userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(User.Status.INACTIVE);
        userRepo.save(user);
        broadcastStatusChange(user);
    }

    public void broadcastStatusChange(User user) {
        var dto = Map.of(
                "type", "STATUS_CHANGE",
                "userId", user.getId(),
                "status", user.getStatus().name());

        List<Long> contacts = contactRepo.findAllFriendIds(user.getId());

        for (Long cId : contacts) {
            messagingTemplate.convertAndSend("/topic/chatlist/" + cId, dto);
        }
    }
}
