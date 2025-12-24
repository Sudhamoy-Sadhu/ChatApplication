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
import java.util.List;
import java.util.Map;

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
                .build();

        Message saved = messageRepository.save(message);

        roomService.updateLastMessage(
                roomId, senderId, preview(content), saved.getSentAt());

        eventPublisher.publishEvent(new MessageSavedEvent(saved.getId()));

        return saved;
    }

    private String preview(String content) {
        if (content == null)
            return null;
        return content.length() > 100 ? content.substring(0, 100) : content;
    }

    public List<Message> getMessages(Long roomId) {
        return messageRepository.findByRoomIdOrderBySentAtAsc(roomId);
    }

    public int getUnreadCount(Long roomId, Long userId) {
        return messageRepository.countUnread(roomId, userId);
    }

    @Transactional
    public void markRoomAsRead(Long roomId, Long userId) {

        // 1. Get IDs of unread messages before marking them
        List<Long> unreadIds = messageRepository.findUnreadIds(roomId, userId);

        if (!unreadIds.isEmpty()) {
            // 2. Mark them as read in DB
            messageRepository.markRoomAsRead(roomId, userId);

            // 3. Find who sent these messages so we can notify them
            List<Object[]> idSenderPairs = messageRepository.findIdsAndSenderForIds(unreadIds);

            // 4. Notify Senders (Distinctly)
            // Instead of a loop, we group by senderId and send one update per sender
            idSenderPairs.stream()
                    .map(pair -> ((Number) pair[1]).longValue()) // Extract senderId
                    .distinct()
                    .forEach(senderId -> {
                        messagingTemplate.convertAndSend(
                                "/topic/receipt/" + senderId,
                                Map.of(
                                        "roomId", roomId,
                                        "status", "READ",
                                        "allMessagesInRoom", true,
                                        "readBy", userId));
                    });
        }

        // 5. Reset Unread Count for the user who just read the room
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/unread",
                new UnreadCountDTO(roomId, 0));
    }

    @Transactional
    public void markAllRoomsAsDelivered(Long userId) {

        List<Message> undelivered = messageRepository.findAllUndelivered(userId);
        if (undelivered.isEmpty())
            return;

        messageRepository.markAllAsDeliveredForUser(userId);

        for (Message m : undelivered) {
            messagingTemplate.convertAndSend(
                    "/topic/receipt/" + m.getSenderId(),
                    java.util.Map.of("messageId", m.getId(), "roomId", m.getRoomId(), "status", "DELIVERED"));
        }
    }

    @Transactional
    public void processAcknowledgment(Long messageId, String status, Long userId) {
        if ("DELIVERED".equals(status)) {
            messageRepository.markDeliveredOne(messageId, userId);
        } else if ("READ".equals(status)) {
            messageRepository.markReadOne(messageId, userId);
        }

        Message message = messageRepository.findWithReceiptsById(messageId).orElse(null);
        if (message == null)
            return;

        messagingTemplate.convertAndSend(
                "/topic/receipt/" + message.getSenderId(),
                java.util.Map.of("messageId", message.getId(), "roomId", message.getRoomId(), "status", status));
    }
}