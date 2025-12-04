package com.example.chat.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chat.DTO.ConnectionRequestDTO.SendRequestDTO;
import com.example.chat.Service.ConnectionRequestService;

import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = ("${cors.allowed-origins}"))
@RequestMapping("/connection")
public class ConnectionRequestController {

    @Autowired
    private ConnectionRequestService requestService;

    @PostMapping("/sendRequest")
    public ResponseEntity<?> sendConnectionRequest(Authentication authentication, @Valid SendRequestDTO sendRequestDTO){
        try {
            Long requestId = Long.valueOf(authentication.getName());
            Long targetId = sendRequestDTO.getTargetId();
            requestService.sendRequest(requestId,targetId);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Request sent successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to send connection request at this moment");
        }
    }
}
