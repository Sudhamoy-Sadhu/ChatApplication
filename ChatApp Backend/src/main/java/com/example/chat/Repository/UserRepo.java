package com.example.chat.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chat.Model.User;

public interface UserRepo extends JpaRepository<User, Long> {
    boolean existsByUsernameOrEmail(String username, String email);

    Optional<User> findByUsernameOrEmail(String username, String email);
    
}
