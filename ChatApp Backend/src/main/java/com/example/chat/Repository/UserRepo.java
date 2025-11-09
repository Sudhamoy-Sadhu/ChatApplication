package com.example.chat.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.chat.Model.User;
@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    boolean existsByUsernameOrEmail(String username, String email);

    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email);

    Optional<User> findByEmail(String email);
    
}
