package com.example.chat.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.chat.Model.Contact;

public interface ContactRepo extends JpaRepository<Contact, Long> {

    List<Contact> findAllByUserId(Long userId);

    Optional<Contact> findByUserIdAndContactUserId(Long userId, Long contactUserId);

    void deleteByUserIdAndContactUserId(Long userId, Long contactUserId);

    @Query("""
                SELECT COUNT(c) > 0
                FROM Contact c
                WHERE c.user.id = :userId AND c.contactUser.id = :otherId
            """)
    boolean existsConnection(@Param("userId") Long userId, @Param("otherId") Long otherId);

    boolean existsByUser_IdAndContactUser_Id(Long userId, Long contactUserId);

    @Query("""
                SELECT c.contactUser.id
                FROM Contact c
                WHERE c.user.id = :userId
                UNION
                SELECT c.user.id
                FROM Contact c
                WHERE c.contactUser.id = :userId
            """)
    List<Long> findAllFriendIds(@Param("userId") Long userId);

}
