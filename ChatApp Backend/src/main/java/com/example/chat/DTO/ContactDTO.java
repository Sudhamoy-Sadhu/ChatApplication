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

    private String username;
    private String email;
    private String profileImageUrl;
    private String status;
    private Instant lastSeen;

    private String lastMessage;
    private String lastMessageTime;

    private Long roomId;       
    private String roomName;    
}
