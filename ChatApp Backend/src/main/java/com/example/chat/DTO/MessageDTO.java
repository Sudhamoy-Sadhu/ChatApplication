package com.example.chat.DTO;

import lombok.*;
import java.time.Instant;
import java.util.List;

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
    private List<Long> readByUserIds;
    private List<Long> deliveredToUserIds;
}
