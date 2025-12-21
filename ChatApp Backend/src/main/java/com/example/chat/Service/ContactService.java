package com.example.chat.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.chat.DTO.ContactDTO;
import com.example.chat.Model.Contact;
import com.example.chat.Model.Room;
import com.example.chat.Model.User;
import com.example.chat.Repository.ContactRepo;
import com.example.chat.Utils.ImageUtils;
import com.example.chat.Utils.TimeFormatter;

@Service
public class ContactService {

    @Autowired
    private ContactRepo contactRepo;

    @Autowired
    private MessageService messageService;

    @Transactional(readOnly = true)
    public List<ContactDTO> getContactsForUser(Long userId) {

        List<Contact> contacts = contactRepo.findAllByUserId(userId);

        return contacts.stream()
                .map(contact -> {
                    User other = contact.getContactUser();
                    Room room = contact.getRoom();
                    int unreadCount = 0;
                    if (room != null) {
                        unreadCount = messageService.getUnreadCount(room.getId(), userId);
                    }

                    return ContactDTO.builder()
                            .id(contact.getId())

                            // USER DETAILS (from contactUser)
                            .userId(other.getId())
                            .username(other.getUsername())
                            .email(other.getEmail())
                            .profilePicture(ImageUtils.getProfilePicture(other.getProfilePicture()))
                            .status(other.getStatus().name())
                            .lastSeen(other.getUpdatedAt())

                            // ROOM DETAILS (from room table)
                            .roomId(room != null ? room.getId() : null)
                            .roomName(room != null ? room.getName() : other.getUsername())
                            .lastMessage(room != null ? room.getLastMessage() : null)
                            .lastMessageTime(room != null ? TimeFormatter.format(room.getLastMessageTime()) : null)
                            .unreadCount(unreadCount)
                            .build();
                })
                .toList();
    }

}
