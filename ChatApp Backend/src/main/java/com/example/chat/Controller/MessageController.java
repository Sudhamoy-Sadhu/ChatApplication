package com.example.chat.Controller;

import com.example.chat.DTO.MessageSendRequestDTO;
import com.example.chat.Model.Message;
import com.example.chat.Service.MessageService;
import com.example.chat.Service.RoomService;
import com.example.chat.Utils.TimeFormatter;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RoomService roomService;

    // ==========================
    // SEND MESSAGE
    // ==========================
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @RequestBody MessageSendRequestDTO request,
            Principal principal) {

        Long senderId = Long.valueOf(principal.getName());
        Long roomId = request.getRoomId();
        String content = request.getContent();

        // Save message in DB
        Message saved = messageService.saveMessage(roomId, senderId, content);

        // Broadcast new message to the chat window (room subscribers)
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId,
                saved
        );

        // Send LAST_MESSAGE update to chatlist listeners
        roomService.getRoomParticipants(roomId)
                .forEach(userId -> {
                    messagingTemplate.convertAndSend(
                            "/topic/chatlist/" + userId,
                            Map.of(
                                    "type", "LAST_MESSAGE",
                                    "roomId", roomId,
                                    "msg", saved.getContent(),
                                    "time", TimeFormatter.format(saved.getSentAt())
                            )
                    );
                });

        return ResponseEntity.ok(saved);
    }

    // ==========================
    // GET ROOM MESSAGES
    // ==========================
    @GetMapping("/{roomId}")
    public ResponseEntity<?> getMessages(@PathVariable Long roomId) {
        List<Message> messages = messageService.getMessages(roomId);
        return ResponseEntity.ok(messages);
    }
}
