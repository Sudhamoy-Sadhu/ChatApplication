package com.example.chat.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.chat.DTO.SignUpDTO;
import com.example.chat.Model.User;
import com.example.chat.Repository.InvitationRepo;
import com.example.chat.Repository.SignUpRepo;

@Service
public class SignUpService {

    @Autowired
    private SignUpRepo signUpRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private InvitationRepo invitationRepo;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public void registerUser(SignUpDTO signUpDTO) {

        if (signUpDTO.getInviteToken() != null) {
            invitationRepo.findByToken(signUpDTO.getInviteToken()).ifPresent(invite -> {
                invite.setAccepted(true);
                invitationRepo.save(invite);

                // notify inviter
                notificationService.notifyUser(
                        invite.getSenderId(),
                        invite.getReceiverName() + " has joined ChatApp using your invitation Link!");
            });
        }

        if (signUpRepo.existsByUsername(signUpDTO.getUsername())) {
            throw new IllegalArgumentException("Username Already exists");
        }
        if (signUpRepo.existsByEmail(signUpDTO.getEmail())) {
            throw new IllegalArgumentException("Email already registered, login to continue");
        }
        if (!signUpDTO.getPassword().equals(signUpDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match!");
        }

        User user = new User();
        user.setUsername(signUpDTO.getUsername());
        user.setEmail(signUpDTO.getEmail());
        user.setPassword(passwordEncoder.encode(signUpDTO.getPassword()));

        signUpRepo.save(user);
    }
}
