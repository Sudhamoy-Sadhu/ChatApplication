package com.example.chat.Event;

import com.example.chat.Model.Message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MessageSavedEvent {
    private final Message message;
}
