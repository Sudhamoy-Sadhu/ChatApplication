package com.example.chat.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chat.DTO.InvitationDTO;
import com.example.chat.Service.InvitationService;
import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = "${cors.allowed-origins}")
@RequestMapping("/invitations")
public class InvitationController {
    
    @Autowired
    private InvitationService invitationService;

    @PostMapping("/sendInvite")
    public ResponseEntity<?> sendInvitationEmail(Authentication authentication, @Valid @RequestBody InvitationDTO invitationDTO) {
        try { 
            String toEmail = invitationDTO.getEmail();
            String name = invitationDTO.getName();
            Long id = Long.valueOf(authentication.getName());
            invitationService.createInvitationEmail(toEmail, name, id);
            return ResponseEntity.ok("Invitation sent successfully to " + toEmail);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("You have already invited this email.");
        }catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred while sending the invitation.");
        }
    }
}
