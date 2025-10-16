package com.example.chat.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chat.DTO.SignUpDTO;
import com.example.chat.Service.SignUpService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/signUp")
@CrossOrigin(origins = "${cors.allowed-origins}")
public class SignUpController {
    
    @Autowired
    private SignUpService signUpService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody SignUpDTO dto) {
        try {
            signUpService.registerUser(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationErrors(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                                .getAllErrors()
                                .get(0)
                                .getDefaultMessage();
        return ResponseEntity.badRequest().body(errorMessage);
    }
}
