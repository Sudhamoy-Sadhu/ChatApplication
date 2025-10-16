package com.example.chat.Controller;

import com.example.chat.Model.ChatMessage;
import com.example.chat.Service.ChatApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@CrossOrigin(origins = "${cors.allowed-origins}")
public class ChatController {

    private final ChatApplicationService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatController(ChatApplicationService roomService, SimpMessagingTemplate messagingTemplate) {
        this.roomService = roomService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.join/{roomId}")
    public void joinRoom(@DestinationVariable String roomId, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        String username = principal.getName();
        roomService.addMember(roomId, username);

        ChatMessage joinMsg = new ChatMessage(
                ChatMessage.MessageType.JOIN,
                username + " joined the room", // content
                username,                      // sender
                roomId,
                LocalDateTime.now(),
                null
        );

        messagingTemplate.convertAndSend("/topic/room/" + roomId, joinMsg);
        messagingTemplate.convertAndSendToUser(username, "/queue/members", roomService.getMembers(roomId));
    }

    @MessageMapping("/chat.leave/{roomId}")
    public void leaveRoom(@DestinationVariable String roomId, Principal principal) {
        String username = principal.getName();
        roomService.removeMember(roomId, username);

        ChatMessage leaveMsg = new ChatMessage(
                ChatMessage.MessageType.LEAVE,
                username + " left the room",
                username,
                roomId,
                LocalDateTime.now(),
                null
        );

        messagingTemplate.convertAndSend("/topic/room/" + roomId, leaveMsg);
    }

    @MessageMapping("/chat.send/{roomId}")
    public void sendMessage(@DestinationVariable String roomId, ChatMessage incoming, Principal principal) {
        String username = principal.getName();

        if (!roomService.isMember(roomId, username)) {
            messagingTemplate.convertAndSendToUser(username, "/queue/errors", "You are not a member of room " + roomId);
            return;
        }

        ChatMessage out = new ChatMessage(
                ChatMessage.MessageType.CHAT,
                incoming.getContent(),
                username,
                roomId,
                LocalDateTime.now(),
                null
        );

        messagingTemplate.convertAndSend("/topic/room/" + roomId, out);
    }

    @MessageMapping("/chat.private")
public void sendPrivateMessage(ChatMessage incoming, Principal principal) {
    String sender = principal.getName();
    String recipient = incoming.getRecipient();

    if (recipient == null || recipient.isEmpty()) {
        // optionally send error back or ignore
        return;
    }

    ChatMessage out = new ChatMessage(
        ChatMessage.MessageType.CHAT,
        incoming.getContent(),
        sender,
        null,  // roomId = null for private messages
        LocalDateTime.now(),
        recipient
    );

    // Send message to recipient's private queue
    messagingTemplate.convertAndSendToUser(recipient, "/queue/messages", out);

    // Optionally, send a copy to sender’s private queue to show sent message
    messagingTemplate.convertAndSendToUser(sender, "/queue/messages", out);
}


    @MessageExceptionHandler
    public void handleException(Throwable exception, Principal principal) {
        if (principal != null) {
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/errors", exception.getMessage());
        }
    }
}
