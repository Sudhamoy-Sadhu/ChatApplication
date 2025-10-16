package com.example.chat.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.chat.Model.Contact;
import com.example.chat.Repository.ContactRepo;

@Service
public class ContactService {
    
    @Autowired
    private ContactRepo contactRepo;

    public List<Contact> getContactsForUser(Long userId) {
        return contactRepo.findAllByUserId(userId);
    }
}
