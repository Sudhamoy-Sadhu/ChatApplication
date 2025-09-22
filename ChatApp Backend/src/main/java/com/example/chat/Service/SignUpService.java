package com.example.chat.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.chat.DTO.SignUpDTO;
import com.example.chat.Model.User;
import com.example.chat.Repository.SignUpRepo;

import jakarta.validation.Valid;

@Service
public class SignUpService {

    @Autowired
    private SignUpRepo signUpRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void registerUser(@Valid SignUpDTO signUpDTO) {
        if (signUpRepo.existsByUsername(signUpDTO.getUsername())) {
            throw new IllegalArgumentException("Username Already exists");
        }
        if (signUpRepo.existsByEmail(signUpDTO.getEmail())) {
            throw new IllegalArgumentException("Email already registered, login to continue");
        }
        if (!signUpDTO.getPassword().equals(signUpDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match!");
        }

        User user = new User();
        user.setUsername(signUpDTO.getUsername());
        user.setEmail(signUpDTO.getEmail());
        user.setPassword(passwordEncoder.encode(signUpDTO.getPassword()));

        signUpRepo.save(user);
    }

}
