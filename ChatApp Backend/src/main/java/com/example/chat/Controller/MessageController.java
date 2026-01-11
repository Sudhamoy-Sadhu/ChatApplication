package com.example.chat.Controller;

import com.example.chat.DTO.MessageDTO;
import com.example.chat.DTO.MessageSendRequestDTO;
import com.example.chat.Model.Message;
import com.example.chat.Service.MessageService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

        private final MessageService messageService;

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
        public ResponseEntity<?> getMessages(@PathVariable Long roomId, Authentication authentication) {
                Long userId = Long.valueOf(authentication.getName());
                List<Message> messages = messageService.getMessages(roomId, userId);
                List<MessageDTO> dtos = messages.stream().map(m -> MessageDTO.builder()
                                .id(m.getId())
                                .senderId(m.getSenderId())
                                .roomId(m.getRoomId())
                                .content(m.getContent())
                                .sentAt(m.getSentAt())
                                .readByUserIds(Set.copyOf(m.getReadByUserIds()))
                                .deliveredToUserIds(Set.copyOf(m.getDeliveredToUserIds()))
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

        @DeleteMapping("/{roomId}/clearChat")
        public ResponseEntity<?> clearChat(@PathVariable Long roomId, Authentication auth) {
                Long userId = Long.valueOf(auth.getName());
                try {
                        messageService.clearChatForUser(roomId, userId);
                        return ResponseEntity.ok("Chat cleared successfully (for you only)");
                } catch (Exception e) {
                        return ResponseEntity.internalServerError().body("Failed to clear chat");
                }
        }
}
