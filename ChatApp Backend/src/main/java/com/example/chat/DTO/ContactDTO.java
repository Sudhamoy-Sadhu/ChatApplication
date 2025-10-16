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
    private String status; // online/offline/busy
    private Instant lastSeen;

    private String lastMessage;
    private Instant lastMessageTime;

    private Long roomId;        // for quick navigation to chatroom
    private String roomName;    // helpful for display (group/private chat name)
}
