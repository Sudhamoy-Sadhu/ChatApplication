package com.example.chat.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; 

    @Column(unique = true, nullable = false)
    private String uniqueKey;

    @Column(nullable = false)
    private Long createdBy;

    private String name;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    private String lastMessage;

    private Long lastMessageSender; 

    private Instant lastMessageTime;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
