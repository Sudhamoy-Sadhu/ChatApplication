package com.example.chat.Service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.chat.Model.Invitation;
import com.example.chat.Repository.InvitationRepo;
import com.example.chat.Repository.UserRepo;

@Service
public class InvitationService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private InvitationRepo invitationRepo;

    public SimpleMailMessage createInvitationEmail(String toEmail, String name, Long id) {

        Optional<Invitation> existing = invitationRepo
                .findBySenderId(id);

        if (existing.isPresent()) {
            throw new RuntimeException("You have already invited this email");
        }

        String token = UUID.randomUUID().toString();
        Invitation invitation = new Invitation();
        invitation.setSenderId(id);
        invitation.setReceiverEmail(toEmail);
        invitation.setReceiverName(name);
        invitation.setToken(token);
        invitationRepo.save(invitation);

        String invitationLink = "http://localhost:3000/login?invite=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        String fromName = userRepo.findById(id).get().getUsername();
        message.setTo(toEmail);
        message.setSubject("Greetings " + name.trim() + " You're Invited to Join ChatApp!");
        message.setText("Hello, " + name + " You have been invited to join ChatApp by " + fromName
                + ". Click the link below to accept the invitation:\n"
                + invitationLink + "\n\nLooking forward to seeing you there!\n\nBest regards,\nChatApp Team");
        message.setFrom("chatapp2400@gmail.com");
        mailSender.send(message);
        System.out.println("✅ Invitation sent to: " + toEmail);
        return message;
    }
}
