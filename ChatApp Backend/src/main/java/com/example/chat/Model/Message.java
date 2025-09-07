package com.example.chat.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Message {
    private Long id;
    private Long room_id;
    private Long sender_id;
    private String content;
    private String sent_at;
}
