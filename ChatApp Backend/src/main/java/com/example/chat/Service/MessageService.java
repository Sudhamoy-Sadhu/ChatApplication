package com.example.chat.Service;

import com.example.chat.Model.Message;
import com.example.chat.Repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final RoomService roomService;

    public Message saveMessage(Long roomId, Long senderId, String content) {

        Message msg = Message.builder()
                .roomId(roomId)
                .senderId(senderId)
                .type(Message.MessageType.TEXT)
                .content(content)
                .sentAt(Instant.now())
                .build();

        Message saved = messageRepository.save(msg);

        // Update last message in room
        roomService.updateLastMessage(roomId, senderId, content);

        return saved;
    }

    public List<Message> getMessages(Long roomId) {
        return messageRepository.findByRoomIdOrderBySentAtAsc(roomId);
    }
    
}
