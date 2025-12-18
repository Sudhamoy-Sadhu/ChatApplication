package com.example.chat.Listener;

import java.util.List;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.chat.Event.MessageSavedEvent;
import com.example.chat.Model.Message;
import com.example.chat.Repository.MessageRepository;
import com.example.chat.Service.RoomService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MessageDeliveryListener {

    private final SimpMessagingTemplate messagingTemplate;
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

        messagingTemplate.convertAndSend("/topic/room/" + m.getRoomId(), messageDTO);

        List<Long> participants = roomService.getRoomParticipants(m.getRoomId());
        for (Long participantId : participants) {
            if (!participantId.equals(m.getSenderId())) {
                messagingTemplate.convertAndSendToUser(
                        participantId.toString(),
                        "/queue/messages",
                        messageDTO);
            }
        }

    }

}
