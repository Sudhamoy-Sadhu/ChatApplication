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
                .readByUserIds(new HashSet<>(List.of(senderId)))
                .deliveredToUserIds(new HashSet<>())
                .build();

        Message saved = messageRepository.save(message);

        roomService.updateLastMessage(
                roomId,
                senderId,
                preview(content),
                saved.getSentAt());

        // 🔑 publish AFTER save
        eventPublisher.publishEvent(new MessageSavedEvent(saved.getId()));

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

        for (Message m : unread) {
            // 1. Auto-correct Delivery Status
            if (!m.getDeliveredToUserIds().contains(userId)) {
                m.getDeliveredToUserIds().add(userId);
            }

            // 2. Mark as Read
            if (!m.getReadByUserIds().contains(userId)) {
                m.getReadByUserIds().add(userId);

                // Notify Sender: "Your message was READ"
                messagingTemplate.convertAndSend(
                        "/topic/receipt/" + m.getSenderId(),
                        Map.of("messageId", m.getId(), "roomId", m.getRoomId(), "status", "READ"));
            }
        }
        messageRepository.saveAll(unread);

        roomService.getRoomParticipants(roomId)
                .forEach(uid -> {
                    messagingTemplate.convertAndSend(
                            "/topic/chatlist/" + uid,
                            Map.of(
                                    "type", "READ_RESET",
                                    "roomId", roomId));
                });

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/unread",
                new UnreadCountDTO(roomId, 0));

    }

    public int getUnreadCount(Long roomId, Long userId) {
        return messageRepository.countUnread(roomId, userId);
    }

    @Transactional
    public void markAllRoomsAsDelivered(Long userId) {

        // 1️⃣ Get all undelivered messages for this user
        List<Message> undelivered = messageRepository.findAllUndelivered(userId);

        if (undelivered.isEmpty())
            return;

        // 2️⃣ Mark delivered safely
        undelivered.forEach(m -> {
            messageRepository.markDeliveredSafe(
                    List.of(m.getRoomId()),
                    userId);
        });

        // 3️⃣ Notify sender PER MESSAGE (this is critical)
        undelivered.forEach(m -> {
            messagingTemplate.convertAndSend(
                    "/topic/receipt/" + m.getSenderId(),
                    Map.of(
                            "messageId", m.getId(),
                            "roomId", m.getRoomId(),
                            "status", "DELIVERED"));
        });
    }

    @Transactional
    public void processAcknowledgment(Long messageId, String status, Long userId) {
        Message message = messageRepository.findById(messageId).orElse(null);
        if (message == null)
            return;

        // Prevent processing if already marked
        if ("DELIVERED".equals(status)) {
            if (message.getDeliveredToUserIds().contains(userId))
                return; // Idempotency check
            message.getDeliveredToUserIds().add(userId);
            messageRepository.markDeliveredOne(messageId, userId);
        } else if ("READ".equals(status)) {
            // If read, it implies delivered too
            if (!message.getDeliveredToUserIds().contains(userId)) {
                message.getDeliveredToUserIds().add(userId);
                messageRepository.markDeliveredOne(messageId, userId);
            }
            if (message.getReadByUserIds().contains(userId))
                return;
            message.getReadByUserIds().add(userId);
            // You might need a custom query for adding read safely or just save:
            messageRepository.save(message);
        }

        // 🚀 REAL-TIME NOTIFICATION TO SENDER
        // This makes the tick change instantly on the sender's screen
        messagingTemplate.convertAndSend(
                "/topic/receipt/" + message.getSenderId(),
                Map.of(
                        "messageId", message.getId(),
                        "roomId", message.getRoomId(),
                        "status", status // "DELIVERED" or "READ"
                ));
    }

}
