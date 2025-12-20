package com.example.chat.DTO;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactDTO {

    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String profilePicture;
    private String status;
    private Instant lastSeen;
    private String lastMessage;
    private String lastMessageTime;
    private int unreadCount;
    private Long roomId;       
    private String roomName;    
}
