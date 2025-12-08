package com.example.chat.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MessageSendRequestDTO {
    private Long roomId;
    private String content;
}
