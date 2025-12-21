package com.example.chat.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.chat.DTO.ForgotPassDTO;
import com.example.chat.DTO.UserResponseDTO;
import com.example.chat.DTO.UserSearchDTO;
import com.example.chat.Model.User;
import com.example.chat.Repository.ContactRepo;
import com.example.chat.Repository.UserRepo;
import com.example.chat.Utils.ImageUtils;

import jakarta.transaction.Transactional;
import net.coobird.thumbnailator.Thumbnails;

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
                            ImageUtils.getProfilePicture(user.getProfilePicture()),
                            true,
                            connected);
                })
                .collect(Collectors.toList());
    }

    public byte[] updateProfilePicture(Long userId, MultipartFile file) throws IOException {
        // 1. Basic Validation (Same as before)
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        // 2. Fetch user
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // 3. WHATSAPP OPTIMIZATION: Compress and Resize
        // We convert the file to a 400x400 square and reduce quality to 70%
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(file.getInputStream())
                .size(400, 400) // Resize to a standard profile size
                .crop(net.coobird.thumbnailator.geometry.Positions.CENTER) // Make it a perfect square
                .outputFormat("jpg") // Force JPG for better compression than PNG
                .outputQuality(0.70) // 70% quality is the sweet spot for web
                .toOutputStream(outputStream);

        byte[] compressedImage = outputStream.toByteArray();

        // 4. Save the optimized version
        user.setProfilePicture(compressedImage);
        userRepo.save(user);

        // Optional: Log the size reduction for your own reference
        System.out.println("Original size: " + file.getSize() / 1024 + " KB");
        System.out.println("Optimized size: " + compressedImage.length / 1024 + " KB");

        return compressedImage;
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

    public UserResponseDTO getProfileData(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                ImageUtils.getProfilePicture(user.getProfilePicture()),
                user.getStatus());
    }

}
