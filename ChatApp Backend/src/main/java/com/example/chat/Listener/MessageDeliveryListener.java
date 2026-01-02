package com.example.chat.Listener;

import java.util.List;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.chat.Event.MessageSavedEvent;
import com.example.chat.Model.Message;
import com.example.chat.Presence.UserSessionRegistry;
import com.example.chat.Repository.MessageRepository;
import com.example.chat.Service.RoomService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MessageDeliveryListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserSessionRegistry registry;
    private final MessageRepository messageRepository;
    private final RoomService roomService;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMessageSaved(MessageSavedEvent event) {

        Message m = messageRepository
                .findById(event.getMessageId())
                .orElseThrow();

        var messageDTO = Map.of(
                "id", m.getId(),
                "senderId", m.getSenderId(),
                "roomId", m.getRoomId(),
                "content", m.getContent(),
                "sentAt", m.getSentAt().toString());

        // Broadcast to room (for UI sync)
        messagingTemplate.convertAndSend(
                "/topic/room/" + m.getRoomId(),
                messageDTO);

        List<Long> participants = roomService.getRoomParticipants(m.getRoomId());

        boolean anyDelivered = false;

        for (Long participantId : participants) {

            if (participantId.equals(m.getSenderId()))
                continue;

            messagingTemplate.convertAndSendToUser(
                    participantId.toString(),
                    "/queue/messages",
                    messageDTO);

            if (registry.isOnline(participantId)) {
                messageRepository.markDeliveredOne(m.getId(), participantId);
                anyDelivered = true;
            }
        }

        if (anyDelivered) {
            messagingTemplate.convertAndSend(
                    "/topic/receipt/" + m.getSenderId(),
                    Map.of(
                            "messageId", m.getId(),
                            "roomId", m.getRoomId(),
                            "status", "DELIVERED"));
        }
    }
}
