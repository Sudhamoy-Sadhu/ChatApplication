package com.example.chat.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.chat.Model.Contact;
import com.example.chat.Repository.ContactRepo;

public class ContactService {
    
    @Autowired
    private ContactRepo contactRepo;

    public List<Contact> getContactsForUser(Long userId) {
        return contactRepo.findAllByUserId(userId);
    }
}
