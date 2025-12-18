package com.example.chat.Controller;

import com.example.chat.Service.MessageService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final MessageService messageService;

    /**
     * Frontend sends this when it receives a message (DELIVERED)
     * or when the user opens the chat (READ).
     */
    @MessageMapping("/chat.ack")
    public void acknowledgeMessage(@Payload AckDTO ack, Principal principal) {
        if (principal == null) return;
        Long userId = Long.valueOf(principal.getName());
        
        messageService.processAcknowledgment(ack.getMessageId(), ack.getStatus(), userId);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AckDTO {
        private Long messageId;
        private String status;
    }
}
