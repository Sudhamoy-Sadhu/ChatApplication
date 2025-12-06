package com.example.chat.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.chat.DTO.ContactDTO;
import com.example.chat.Model.Contact;
import com.example.chat.Model.Room;
import com.example.chat.Model.User;
import com.example.chat.Repository.ContactRepo;
import com.example.chat.Utils.TimeFormatter;

@Service
public class ContactService {

    @Autowired
    private ContactRepo contactRepo;

    public List<ContactDTO> getContactsForUser(Long userId) {

        List<Contact> contacts = contactRepo.findAllByUserId(userId);

        return contacts.stream()
                .map(contact -> {
                    User other = contact.getContactUser();
                    Room room = contact.getRoom();

                    return ContactDTO.builder()
                            .id(contact.getId())

                            // USER DETAILS (from contactUser)
                            .username(other.getUsername())
                            .email(other.getEmail())
                            .profileImageUrl(other.getProfilePicture() != null ? "some-url" : null)
                            .status(other.getStatus().name())
                            .lastSeen(other.getUpdatedAt())

                            // ROOM DETAILS (from room table)
                            .roomId(room != null ? room.getId() : null)
                            .roomName(room != null ? room.getName() : other.getUsername())
                            .lastMessage(room != null ? room.getLastMessage() : null)
                            .lastMessageTime(room != null ? TimeFormatter.format(room.getLastMessageTime()) : null)

                            .build();
                })
                .toList();
    }

}
