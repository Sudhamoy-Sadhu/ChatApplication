package com.example.chat.Controller;

import com.example.chat.DTO.MessageDTO;
import com.example.chat.DTO.MessageSendRequestDTO;
import com.example.chat.Model.Message;
import com.example.chat.Service.MessageService;
import com.example.chat.Service.RoomService;
import com.example.chat.Utils.TimeFormatter;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
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
        public ResponseEntity<MessageDTO> sendMessage(
                        @RequestBody MessageSendRequestDTO request,
                        Principal principal) {

                Long senderId = Long.valueOf(principal.getName());

                Message saved = messageService.saveMessage(
                                request.getRoomId(),
                                senderId,
                                request.getContent());

                MessageDTO dto = MessageDTO.builder()
                                .id(saved.getId())
                                .senderId(saved.getSenderId())
                                .roomId(saved.getRoomId())
                                .content(saved.getContent())
                                .sentAt(saved.getSentAt())
                                .build();

                return ResponseEntity.ok(dto);
        }

        // ==========================
        // GET ROOM MESSAGES
        // ==========================
        @GetMapping("/{roomId}")
        public ResponseEntity<?> getMessages(@PathVariable Long roomId) {
                List<Message> messages = messageService.getMessages(roomId);

                List<MessageDTO> dtos = messages.stream().map(m -> MessageDTO.builder()
                                .id(m.getId())
                                .senderId(m.getSenderId())
                                .roomId(m.getRoomId())
                                .content(m.getContent())
                                .sentAt(m.getSentAt())
                                .build()).toList();

                return ResponseEntity.ok(dtos);
        }

        @PostMapping("/{roomId}/mark-read")
        public ResponseEntity<?> markAsRead(
                        @PathVariable Long roomId,
                        Authentication authentication) {
                Long userId = Long.valueOf(authentication.getName());
                messageService.markRoomAsRead(roomId, userId);
                return ResponseEntity.ok().build();
        }

}
