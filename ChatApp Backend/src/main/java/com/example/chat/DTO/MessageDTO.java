package com.example.chat.DTO;

import lombok.*;
import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {
    private Long id;
    private Long senderId;
    private Long roomId;
    private String content;
    private Instant sentAt;
    private Set<Long> readByUserIds;
    private Set<Long> deliveredToUserIds;
}
