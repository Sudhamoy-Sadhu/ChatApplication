package com.example.chat.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.chat.DTO.ForgotPassDTO;
import com.example.chat.DTO.UserResponseDTO;
import com.example.chat.DTO.UserSearchDTO;
import com.example.chat.Model.User;
import com.example.chat.Repository.ContactRepo;
import com.example.chat.Repository.UserRepo;
import com.example.chat.Utils.ImageUtils;
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

@Transactional
public byte[] updateProfilePicture(Long userId, MultipartFile file) throws IOException {
   
    if (file == null || file.isEmpty()) {
        throw new IllegalArgumentException("File cannot be empty");
    }

    User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

    byte[] finalImageBytes;

    try {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(file.getInputStream())
                .size(400, 400)
                .crop(net.coobird.thumbnailator.geometry.Positions.CENTER)
                .outputFormat("jpg") // Converts WebP/PNG/etc to JPG
                .outputQuality(0.70)
                .toOutputStream(outputStream);

        finalImageBytes = outputStream.toByteArray();
        
        System.out.println("Optimization successful. Format: " + file.getContentType());
    } catch (Exception e) {
        // 4. FALLBACK: If Thumbnailator still fails, save original bytes so the app doesn't crash
        System.err.println("Thumbnailator failed to process format: " + file.getContentType() + ". Saving original instead.");
        e.printStackTrace();
        finalImageBytes = file.getBytes();
    }

    user.setProfilePicture(finalImageBytes);
    
    userRepo.saveAndFlush(user);

    System.out.println("Original size: " + file.getSize() / 1024 + " KB");
    System.out.println("Final size: " + finalImageBytes.length / 1024 + " KB");

    broadcastProfileUpdate(user);
    
    return finalImageBytes;
}

    private void broadcastProfileUpdate(User user) {
        var dto = Map.of(
                "type", "PROFILE_UPDATE",
                "userId", user.getId(),
                "profilePicture", ImageUtils.getProfilePicture(user.getProfilePicture()));

        List<Long> contacts = contactRepo.findAllFriendIds(user.getId());
        for (Long cId : contacts) {
            messagingTemplate.convertAndSend("/topic/chatlist/" + cId, dto);
        }
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
