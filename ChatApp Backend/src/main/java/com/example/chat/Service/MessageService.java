package com.example.chat.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.chat.DTO.UnreadCountDTO;
import com.example.chat.Event.MessageSavedEvent;
import com.example.chat.Model.Message;
import com.example.chat.Model.RoomClearance;
import com.example.chat.Repository.MessageRepository;
import com.example.chat.Repository.RoomClearanceRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RoomClearanceRepository roomClearanceRepository;

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

    public List<Message> getMessages(Long roomId, Long userId) {
        RoomClearance clearance = roomClearanceRepository.findByRoomIdAndUserId(roomId, userId)
                .orElse(null);

        Instant minDate = (clearance != null) ? clearance.getClearedAt() : Instant.EPOCH;

        return messageRepository.findByRoomIdWithReceiptsAfter(roomId, minDate);
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
    public void processAcknowledgment(Long messageId, String status, Long userId) {

        Message message = messageRepository.findWithReceiptsById(messageId).orElse(null);
        if (message == null)
            return;

        if (message.getSenderId().equals(userId))
            return;

        if ("READ".equals(status) && !message.getSenderId().equals(userId)) {
            messageRepository.markReadOne(messageId, userId);
        } else if ("DELIVERED".equals(status) && !message.getSenderId().equals(userId)) {
            messageRepository.markDeliveredOne(messageId, userId);
        }

        messagingTemplate.convertAndSend(
                "/topic/receipt/" + message.getSenderId(),
                java.util.Map.of("messageId", message.getId(), "roomId", message.getRoomId(), "status", status));
    }

    @Transactional
    public void deliverPendingMessages(Long userId) {
        // 1. Find senders who are waiting for a delivery receipt from this user
        List<Long> sendersToNotify = messageRepository.findSendersWithPendingDeliveries(userId);

        if (sendersToNotify.isEmpty())
            return;

        // 2. Bulk mark all messages as DELIVERED for this user in DB
        // (You already have this native query)
        messageRepository.markAllAsDeliveredForUser(userId);

        // 3. Notify each sender via WebSocket
        sendersToNotify.forEach(senderId -> {
            messagingTemplate.convertAndSend(
                    "/topic/receipt/" + senderId,
                    Map.of(
                            "status", "DELIVERED",
                            "deliveredTo", userId,
                            "allPending", true // Custom flag to tell frontend to mark all SENT as DELIVERED
            ));
        });
    }

    @Transactional
    public void clearChatForUser(Long roomId, Long userId) {
        RoomClearance clearance = roomClearanceRepository.findByRoomIdAndUserId(roomId, userId)
                .orElse(RoomClearance.builder()
                        .roomId(roomId)
                        .userId(userId)
                        .build());
        
        clearance.setClearedAt(Instant.now());
        roomClearanceRepository.save(clearance);

        // =====================================================
        // 2. REAL-TIME WEBSOCKET UPDATES
        // =====================================================

        // A. Notify the Open Chat Window to clear messages
        Map<String, Object> clearEvent = new HashMap<>();
        clearEvent.put("type", "CHAT_CLEARED");
        clearEvent.put("roomId", roomId);

        messagingTemplate.convertAndSendToUser(
                userId.toString(), 
                "/queue/chat-events", 
                clearEvent
        );

        // B. Notify the Chat List (Sidebar) to clear the "Last Message" preview
        Map<String, Object> listUpdate = new HashMap<>();
        listUpdate.put("type", "LAST_MESSAGE");
        listUpdate.put("roomId", roomId);
        listUpdate.put("msg", ""); 
        listUpdate.put("time", null);
        listUpdate.put("formattedTime", "");

        messagingTemplate.convertAndSend(
                "/topic/chatlist/" + userId,
                listUpdate
        );
    }
}