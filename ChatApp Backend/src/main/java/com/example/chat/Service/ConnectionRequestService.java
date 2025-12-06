package com.example.chat.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.chat.Model.ConnectionRequest;
import com.example.chat.Model.Contact;
import com.example.chat.Model.Room;
import com.example.chat.Model.User;
import com.example.chat.Repository.ConnectionRequestRepo;
import com.example.chat.Repository.ContactRepo;
import com.example.chat.Repository.UserRepo;

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

    public void sendRequest(Long requesterId, Long targetId) {

        if (requesterId.equals(targetId)) {
            throw new RuntimeException("You cannot send request to yourself");
        }

        if (connectionRequestRepo.findByRequesterId_IdAndTargetId_Id(requesterId, targetId).isPresent()
                || connectionRequestRepo.findByRequesterId_IdAndTargetId_Id(targetId, requesterId).isPresent()) {
            throw new RuntimeException("Connection Request Already Exists!");
        }

        User requester = userRepo.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        User target = userRepo.findById(targetId)
                .orElseThrow(() -> new RuntimeException("Requested User Not Found"));

        ConnectionRequest request = new ConnectionRequest();
        request.setRequesterId(requester);
        request.setTargetId(target);
        request.setStatus(ConnectionRequest.Status.PENDING);

        connectionRequestRepo.save(request);
    }

    public ConnectionRequest.Status getRequestStatus(Long requesterId, Long targetId) {

        return connectionRequestRepo.findByRequesterId_IdAndTargetId_Id(requesterId, targetId)
                .map(ConnectionRequest::getStatus)
                .orElse(ConnectionRequest.Status.NONE);
    }

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
            requester.put("profilePic", req.getRequesterId().getProfilePicture());

            Map<String, Object> target = new HashMap<>();
            target.put("id", req.getTargetId().getId());
            target.put("name", req.getTargetId().getUsername());
            target.put("email", req.getTargetId().getEmail());
            target.put("profilePic", req.getTargetId().getProfilePicture());

            dto.put("requester", requester);
            dto.put("target", target);

            return dto;
        }).toList();
    }

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

        contactRepo.save(c1);
        // CONTACT 2 → requester adds target
        Contact c2 = Contact.builder()
                .user(requester)
                .contactUser(target)
                .room(room)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        contactRepo.save(c2);
    }

    public void rejectConnectionRequest(Long requestId) {
        ConnectionRequest request = connectionRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setStatus(ConnectionRequest.Status.REJECTED);
        request.setUpdatedAt(Instant.now());

        connectionRequestRepo.save(request);
    }

    public void cancelConnectionRequest(Long requestId, Long loggedInUserId) {

        ConnectionRequest request = connectionRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getRequesterId().getId().equals(loggedInUserId)) {
            throw new RuntimeException("You are not allowed to cancel this request");
        }

        connectionRequestRepo.delete(request);
    }

}
