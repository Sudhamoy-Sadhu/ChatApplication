package com.example.chat.Service;

import com.example.chat.DTO.UnreadCountDTO;
import com.example.chat.Event.MessageSavedEvent;
import com.example.chat.Model.Message;
import com.example.chat.Repository.MessageRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Message saveMessage(Long roomId, Long senderId, String content) {

        Message message = Message.builder()
                .roomId(roomId)
                .senderId(senderId)
                .type(Message.MessageType.TEXT)
                .content(content)
                .sentAt(Instant.now())
                .readByUserIds(new HashSet<>(List.of(senderId)))
                .build();

        Message saved = messageRepository.save(message);

        roomService.updateLastMessage(
                roomId,
                senderId,
                preview(content),
                saved.getSentAt());

        // 🔑 publish AFTER save
        eventPublisher.publishEvent(new MessageSavedEvent(saved));

        return saved;
    }

    private String preview(String content) {
        if (content == null)
            return null;
        return content.length() > 100
                ? content.substring(0, 100)
                : content;
    }

    public List<Message> getMessages(Long roomId) {
        return messageRepository.findByRoomIdOrderBySentAtAsc(roomId);
    }

    @Transactional
    public void markRoomAsRead(Long roomId, Long userId) {
        List<Message> unread = messageRepository.findUnreadMessages(roomId, userId);
        unread.forEach(m -> m.getReadByUserIds().add(userId));
        messageRepository.saveAll(unread);
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/unread",
                new UnreadCountDTO(roomId, 0));

    }

    public int getUnreadCount(Long roomId, Long userId) {
        return messageRepository.countUnread(roomId, userId);
    }
}
