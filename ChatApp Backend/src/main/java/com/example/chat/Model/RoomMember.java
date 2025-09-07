package com.example.chat.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomMember {

    private Long id;
    private Long room_id;
    private Long user_id;
    private String joined_at;
}
