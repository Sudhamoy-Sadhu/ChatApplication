package com.example.chat.Model;

import java.time.Instant;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    public enum MessageType {
        TEXT, IMAGE, AUDIO, VIDEO, FILE, CALL_OFFER, CALL_ANSWER, CALL_ICE, CALL_EVENT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type = MessageType.TEXT;

    // ==============================================
    // SIMPLE CHAT IMPLEMENTATION (CURRENT WORKING)
    // ==============================================
    @Column(columnDefinition = "TEXT")
    private String content; // plain text message (used by your current service)

    // ==============================================
    // FUTURE ENCRYPTION FIELDS (COMMENTED)
    // ==============================================
    /*
     * @Lob
     * 
     * @Column(columnDefinition = "TEXT")
     * private String ciphertext; // encrypted message payload
     * 
     * private String cipherAlg; // e.g., XCHACHA20_POLY1305
     * private String nonce; // iv/nonce
     * private String ephemeralPubKey; // for E2E key exchange
     * private String tag; // auth tag (optional)
     */

    // ==============================================
    // FUTURE MEDIA SUPPORT (COMMENTED)
    // ==============================================
    /*
     * private String mediaUrl;
     * private String mimeType;
     * private Long sizeBytes;
     * private Integer durationSeconds;
     */

    @ElementCollection
    Set<Long> readByUserIds;

    @ElementCollection
    @CollectionTable(name = "message_delivered", joinColumns = @JoinColumn(name = "message_id"))
    @Column(name = "user_id")
    private Set<Long> deliveredToUserIds;

    @Column(nullable = false)
    private Instant sentAt = Instant.now();
}