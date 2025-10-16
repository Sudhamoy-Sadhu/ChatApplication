package com.example.chat.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chat.Model.User;

public interface UserRepo extends JpaRepository<User, Long> {
    boolean existsByUsernameOrEmail(String username, String email);

    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email);

    
}
