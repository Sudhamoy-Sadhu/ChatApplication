package com.example.chat.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.chat.Repository.InvitationRepo;
import com.example.chat.Repository.UserRepo;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private InvitationRepo invitationRepo;

    public SimpleMailMessage notifyUser(Long userId, String message) {
        // Implementation for notifying the user (e.g., sending an email or in-app notification)
        // This is a placeholder implementation

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        String userEmail = userRepo.findEmailById(userId).get().getEmail();
        String NameofAcceptedUser = invitationRepo.findBySenderId(userId).get().getReceiverName();
        mailMessage.setTo(userEmail);
        mailMessage.setSubject(NameofAcceptedUser+" Accepted your Invitation!");
        mailMessage.setText("Greetings! \n\n"+message+"\nStart chatting by sending a connection Request.\n\nBest regards,\nChatApp Team");
        mailMessage.setFrom("chatapp2400@gmail.com");
        mailSender.send(mailMessage);
        System.out.println("Notifying user " + userId + ": " + message);
        return mailMessage;
    }
}
