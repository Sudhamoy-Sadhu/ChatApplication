package com.example.chat.Controller;

import com.example.chat.Model.Message;
import com.example.chat.Service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // ==========================
    // SEND MESSAGE
    // ==========================
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @RequestBody Map<String, String> body,
            Principal principal
    ) {
        Long roomId = Long.valueOf(body.get("roomId"));
        String content = body.get("content");

        Long senderId = Long.valueOf(principal.getName()); // Extract authenticated user ID

        Message saved = messageService.saveMessage(roomId, senderId, content);

        return ResponseEntity.ok(saved);
    }

    // ==========================
    // FETCH MESSAGES FOR A ROOM
    // ==========================
    @GetMapping("/{roomId}")
    public ResponseEntity<?> getMessages(@PathVariable Long roomId) {
        List<Message> messages = messageService.getMessages(roomId);
        return ResponseEntity.ok(messages);
    }
}
