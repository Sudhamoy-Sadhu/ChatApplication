package com.example.chat.Service;

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

        if (connectionRequestRepo.findByRequesterIdIdAndTargetIdId(requesterId, targetId).isPresent()) {
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
}
