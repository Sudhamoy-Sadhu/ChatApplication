package com.example.chat.Listener;

import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.chat.DTO.MessageDTO;
import com.example.chat.DTO.UnreadCountDTO;
import com.example.chat.Event.MessageSavedEvent;
import com.example.chat.Model.Message;
import com.example.chat.Repository.MessageRepository;
import com.example.chat.Service.RoomService;
import com.example.chat.Utils.TimeFormatter;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MessageBroadcastListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomService roomService;
    private final MessageRepository messageRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageSaved(MessageSavedEvent event) {

        // ✅ Use the new method to fetch with collections eagerly
        Message m = messageRepository
                .findWithReceiptsById(event.getMessageId())
                .orElseThrow(() -> new IllegalStateException("Message not found"));

        MessageDTO dto = MessageDTO.builder()
                .id(m.getId())
                .roomId(m.getRoomId())
                .senderId(m.getSenderId())
                .content(m.getContent())
                .sentAt(m.getSentAt())
                .readByUserIds(m.getReadByUserIds())          // ✅ Populated
                .deliveredToUserIds(m.getDeliveredToUserIds()) // ✅ Populated
                .build();

        // 1️⃣ Chat window
        messagingTemplate.convertAndSend(
                "/topic/room/" + m.getRoomId(),
                dto);

        // 2️⃣ Unread count update
        roomService.getRoomParticipants(m.getRoomId())
                .forEach(userId -> {
                    if (!userId.equals(m.getSenderId())) {

                        int unreadCount = messageRepository.countUnread(
                                m.getRoomId(), userId);

                        messagingTemplate.convertAndSendToUser(
                                userId.toString(),
                                "/queue/unread",
                                new UnreadCountDTO(m.getRoomId(), unreadCount));
                    }
                });

        // 3️⃣ Chat list last message update
        roomService.getRoomParticipants(m.getRoomId())
                .forEach(userId -> {
                    messagingTemplate.convertAndSend(
                            "/topic/chatlist/" + userId,
                            Map.of(
                                    "type", "LAST_MESSAGE",
                                    "roomId", m.getRoomId(),
                                    "msg", roomPreview(m.getContent()),
                                    "time", m.getSentAt(),
                                    "formattedTime", TimeFormatter.format(m.getSentAt())));
                });
    }

    private String roomPreview(String content) {
        if (content == null)
            return "";
        return content.length() > 100 ? content.substring(0, 100) : content;
    }
}