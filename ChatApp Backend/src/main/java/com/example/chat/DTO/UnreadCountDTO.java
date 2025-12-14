package com.example.chat.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UnreadCountDTO {
    private Long roomId;
    private int unreadCount;
}
