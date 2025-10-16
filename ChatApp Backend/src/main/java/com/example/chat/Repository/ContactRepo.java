package com.example.chat.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chat.Model.Contact;

public interface ContactRepo extends JpaRepository<Contact, Long> {
    List<Contact> findAllByUserId(Long userId);
    Optional<Contact> findByUserIdAndContactUserId(Long userId, Long contactUserId);
    void deleteByUserIdAndContactUserId(Long userId, Long contactUserId);
}
