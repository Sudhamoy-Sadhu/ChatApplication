package com.example.chat.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.chat.Model.ConnectionRequest;
import com.example.chat.Model.Contact;
import com.example.chat.Model.Room;
import com.example.chat.Model.User;
import com.example.chat.Repository.ConnectionRequestRepo;
import com.example.chat.Repository.ContactRepo;
import com.example.chat.Repository.UserRepo;
import com.example.chat.Utils.ImageUtils;

@Service
public class ConnectionRequestService {

    @Autowired
    private ConnectionRequestRepo connectionRequestRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ContactRepo contactRepo;
    @Autowired
    private RoomService roomService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendRequest(Long requesterId, Long targetId) {

        if (requesterId.equals(targetId)) {
            throw new RuntimeException("You cannot send request to yourself");
        }

        Optional<ConnectionRequest> existingOpt = connectionRequestRepo.findBetweenUsers(requesterId, targetId);

        if (existingOpt.isPresent()) {

            ConnectionRequest existing = existingOpt.get();

            switch (existing.getStatus()) {

                case PENDING -> {
                    throw new RuntimeException(
                            "You have already sent a connection request. Please wait for a response.");
                }

                case ACCEPTED -> {
                    throw new RuntimeException(
                            "You are already connected with this user.");
                }

                case REJECTED -> {
                    Instant rejectedAt = existing.getUpdatedAt();
                    long daysPassed = ChronoUnit.DAYS.between(rejectedAt, Instant.now());
                    long cooldownDays = 7;

                    if (daysPassed < cooldownDays) {
                        long daysLeft = cooldownDays - daysPassed;
                        boolean rejectedByTarget = existing.getTargetId().getId().equals(targetId);
                        if (rejectedByTarget) {
                            throw new RuntimeException(
                                    "Your previous request was rejected. You can send a new request after "
                                            + daysLeft + " day(s).");
                        } else {
                            throw new RuntimeException(
                                    "You have rejected this request. Try connecting with the user after "
                                            + daysLeft + " day(s).");
                        }
                    }

                    // If 7 days passed → allow new request
                    // (Old rejected request will be cleaned by scheduler)
                }
            }
        }

        User requester = userRepo.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        User target = userRepo.findById(targetId)
                .orElseThrow(() -> new RuntimeException("Requested User Not Found"));

        ConnectionRequest request = new ConnectionRequest();
        request.setRequesterId(requester);
        request.setTargetId(target);
        request.setStatus(ConnectionRequest.Status.PENDING);
        request.setSeen(false);
        request.setCreatedAt(Instant.now());

        connectionRequestRepo.save(request);

        messagingTemplate.convertAndSend(
                "/topic/chatlist/" + target.getId(),
                Map.of(
                        "type", "NEW_CONNECTION_REQUEST",
                        "requestId", request.getId(),
                        "fromUser", Map.of(
                                "id", requester.getId(),
                                "username", requester.getUsername(),
                                "email", requester.getEmail(),
                                "profilePicture", ImageUtils.getProfilePicture(requester.getProfilePicture()))));
    }

    public ConnectionRequest.Status getRequestStatus(Long requesterId, Long targetId) {

        return connectionRequestRepo.findByRequesterId_IdAndTargetId_Id(requesterId, targetId)
                .map(ConnectionRequest::getStatus)
                .orElse(ConnectionRequest.Status.NONE);
    }

    @Transactional
    public List<Map<String, Object>> getAllConnections(Long userId) {
        List<ConnectionRequest> connections = connectionRequestRepo.findAllRelatedToUser(userId);

        return connections.stream().map(req -> {
            Map<String, Object> dto = new HashMap<>();

            dto.put("requestId", req.getId());
            dto.put("status", req.getStatus().name());
            dto.put("createdAt", req.getCreatedAt());
            dto.put("updatedAt", req.getUpdatedAt());

            Map<String, Object> requester = new HashMap<>();
            requester.put("id", req.getRequesterId().getId());
            requester.put("name", req.getRequesterId().getUsername());
            requester.put("email", req.getRequesterId().getEmail());
            requester.put("profilePicture", ImageUtils.getProfilePicture(req.getRequesterId().getProfilePicture()));

            Map<String, Object> target = new HashMap<>();
            target.put("id", req.getTargetId().getId());
            target.put("name", req.getTargetId().getUsername());
            target.put("email", req.getTargetId().getEmail());
            target.put("profilePicture", ImageUtils.getProfilePicture(req.getTargetId().getProfilePicture()));

            dto.put("requester", requester);
            dto.put("target", target);

            return dto;
        }).toList();
    }

    @Transactional
    public void acceptConnectionRequest(Long requestId) {

        ConnectionRequest request = connectionRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        User requester = request.getRequesterId();
        User target = request.getTargetId();

        // Update request status
        request.setStatus(ConnectionRequest.Status.ACCEPTED);
        request.setUpdatedAt(Instant.now());
        connectionRequestRepo.save(request);

        // Create room for chat (optional)
        Room room = roomService.getOrCreatePrivateRoom(
                requester.getId(),
                target.getId());

        // CONTACT 1 → target adds requester
        Contact c1 = Contact.builder()
                .user(target)
                .contactUser(requester)
                .room(room)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // CONTACT 2 → requester adds target
        Contact c2 = Contact.builder()
                .user(requester)
                .contactUser(target)
                .room(room)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        if (!contactRepo.existsBetweenUsers(requester.getId(), target.getId())) {
            contactRepo.save(c1);
            contactRepo.save(c2);
        }

        notifyContactAdded(requester, target, room);
        notifyContactAdded(target, requester, room);

        messagingTemplate.convertAndSend(
                "/topic/chatlist/" + target.getId(),
                Map.of("type", "REQUEST_COUNT_DECREMENT", "by", 1));

    }

    private void notifyContactAdded(User owner, User newContact, Room room) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "CONTACT_ADDED");

        payload.put("contact", Map.of(
                "userId", newContact.getId(),
                "username", newContact.getUsername(),
                "email", newContact.getEmail(),
                "profilePicture", ImageUtils.getProfilePicture(newContact.getProfilePicture()),
                "roomId", room.getId(),
                "roomName", newContact.getUsername(),
                "status", newContact.getStatus().name(),
                "lastMessage", "Start your conversation!",
                "lastMessageTime", "",
                "unreadCount", 0));

        messagingTemplate.convertAndSend(
                "/topic/chatlist/" + owner.getId(),
                payload);
    }

    @Transactional
    public void rejectConnectionRequest(Long requestId) {

        ConnectionRequest request = connectionRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        User requester = request.getRequesterId();
        User target = request.getTargetId();

        request.setStatus(ConnectionRequest.Status.REJECTED);
        request.setSeen(true);
        request.setUpdatedAt(Instant.now());
        connectionRequestRepo.save(request);

        messagingTemplate.convertAndSend(
                "/topic/chatlist/" + requester.getId(),
                Map.of("type", "REQUEST_REJECTED", "requestId", requestId));

        messagingTemplate.convertAndSend(
                "/topic/chatlist/" + target.getId(),
                Map.of("type", "REQUEST_COUNT_DECREMENT", "by", 1));
    }

    public void cancelConnectionRequest(Long requestId, Long loggedInUserId) {

        ConnectionRequest request = connectionRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getRequesterId().getId().equals(loggedInUserId)) {
            throw new RuntimeException("You are not allowed to cancel this request");
        }

        if (!request.isSeen()) {
            messagingTemplate.convertAndSend(
                    "/topic/chatlist/" + request.getTargetId().getId(),
                    Map.of("type", "REQUEST_COUNT_DECREMENT", "by", 1));
        }

        connectionRequestRepo.delete(request);
    }

    public long getUnreadRequestCount(Long userId) {
        return connectionRequestRepo.countUnreadRequests(userId);
    }

    @Transactional
    public void markRequestsAsSeen(Long userId) {

        List<ConnectionRequest> unread = connectionRequestRepo.findUnreadRequests(userId);

        unread.forEach(req -> {
            req.setSeen(true);
            req.setUpdatedAt(Instant.now());
        });

        connectionRequestRepo.saveAll(unread);
    }

}
