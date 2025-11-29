package com.example.chat.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chat.Model.Invitation;

public interface InvitationRepo extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByToken(String token);

    Optional<Invitation> findBySenderId(Long senderId);

}