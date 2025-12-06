package com.example.chat.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

import com.example.chat.DTO.ContactDTO;
import com.example.chat.Service.ContactService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@CrossOrigin(origins = "${cors.allowed-origins}")
@RequestMapping("/contacts")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @GetMapping("/allContacts")
    public ResponseEntity<?> getAllContacts(Authentication authentication) {
        try {
            Long userId = Long.valueOf(authentication.getName());

            List<ContactDTO> contacts = contactService.getContactsForUser(userId);

            return ResponseEntity.ok(contacts);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch contacts");
        }
    }

}
