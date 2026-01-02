package com.example.chat.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chat.Model.ConnectionRequest;
import com.example.chat.Service.ConnectionRequestService;

@RestController
@CrossOrigin(origins = ("${cors.allowed-origins}"))
@RequestMapping("/connection")
public class ConnectionRequestController {

    @Autowired
    private ConnectionRequestService requestService;

    @PostMapping("/sendRequest/{targetId}")
    public ResponseEntity<?> sendConnectionRequest(
            Authentication authentication,
            @PathVariable Long targetId) {
        try {
            Long requesterId = Long.valueOf(authentication.getName());
            requestService.sendRequest(requesterId, targetId);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body("Request sent successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to send connection request at this moment");
        }
    }

    @GetMapping("/status/{requesterId}/{targetId}")
    public ResponseEntity<?> getStatus(
            @PathVariable Long requesterId,
            @PathVariable Long targetId) {

        try {
            ConnectionRequest.Status status = requestService.getRequestStatus(requesterId, targetId);
            return ResponseEntity.ok(status);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong while fetching request status");
        }
    }

    @GetMapping("/getAllConnections")
    public ResponseEntity<?> getAllConnectionRequests(Authentication auth) {
        try {
            Long userId = Long.valueOf(auth.getName());
            return ResponseEntity.ok(requestService.getAllConnections(userId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Unable to fetch connection requests");
        }
    }

    @PutMapping("/accept/{requestId}")
    public ResponseEntity<?> acceptConnectionRequest(Authentication authentication, @PathVariable Long requestId) {
        try {
            requestService.acceptConnectionRequest(requestId);
            return ResponseEntity.ok("Accepted Connection Request");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to accept connection request");
        }
    }

    @PutMapping("/reject/{requestId}")
    public ResponseEntity<?> rejectConnectionRequest(Authentication authentication, @PathVariable Long requestId) {
        try {
            requestService.rejectConnectionRequest(requestId);
            return ResponseEntity.ok("Rejected Connection Request");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to accept connection request");
        }
    }

    @DeleteMapping("/cancel/{requestId}")
    public ResponseEntity<?> cancelConnectionRequest(
            Authentication authentication,
            @PathVariable Long requestId) {

        try {
            Long loggedInUserId = Long.valueOf(authentication.getName());

            requestService.cancelConnectionRequest(requestId, loggedInUserId);

            return ResponseEntity.ok("Cancelled Connection Request");

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to cancel connection request");
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(Authentication authentication) {

        try{
            Long userId = Long.valueOf(authentication.getName());
            Long unreadCount = requestService.getUnreadRequestCount(userId);
            return ResponseEntity.ok(unreadCount);

        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
