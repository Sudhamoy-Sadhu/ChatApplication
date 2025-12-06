package com.example.chat.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.chat.Model.ConnectionRequest;
import com.example.chat.Model.User;
import com.example.chat.Repository.ConnectionRequestRepo;
import com.example.chat.Repository.UserRepo;

@Service
public class ConnectionRequestService {

    @Autowired
    private ConnectionRequestRepo connectionRequestRepo;
    @Autowired
    private UserRepo userRepo;

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

            // requester
            Map<String, Object> requester = new HashMap<>();
            requester.put("id", req.getRequesterId().getId());
            requester.put("name", req.getRequesterId().getUsername());
            requester.put("email", req.getRequesterId().getEmail());
            requester.put("profilePic", req.getRequesterId().getProfilePicture());

            // target
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

}
