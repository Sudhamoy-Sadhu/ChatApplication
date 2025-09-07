package com.example.chat.Model;

import java.time.LocalDateTime;

// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// @Entity
// @Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    
    // @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    public enum MessageType {
        CHAT, JOIN, LEAVE
    }

    private MessageType type;
    private String content;
    private String sender;
    private String roomId;
    private LocalDateTime timestamp;
    private String recipient;

}
