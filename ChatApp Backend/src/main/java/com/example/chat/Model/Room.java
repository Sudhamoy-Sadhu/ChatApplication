package com.example.chat.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    private Long id;
    private String name;
    private String type;
    private String unique_key;
    private String created_by;
    private String created_at;
}
