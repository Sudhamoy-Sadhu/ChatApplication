package com.example.chat.Event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MessageSavedEvent {
    private final Long messageId;
}
