package com.example.chat.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.chat.Model.ConnectionRequest;
import com.example.chat.Model.User;

@Repository
public interface ConnectionRequestRepo extends JpaRepository<ConnectionRequest, Long> {

    List<ConnectionRequest> findByTargetId_IdAndStatus(Long targetId, ConnectionRequest.Status status);

    Optional<ConnectionRequest> findByRequesterIdAndTargetId(User requester, User target);

    Optional<ConnectionRequest> findByRequesterId_IdAndTargetId_Id(Long requesterId, Long targetId);

    @Query("SELECT cr FROM ConnectionRequest cr WHERE cr.requesterId.id = :id OR cr.targetId.id = :id")
    List<ConnectionRequest> findAllRelatedToUser(Long id);

    @Query("""
            SELECT COUNT(cr)
            FROM ConnectionRequest cr
            WHERE cr.targetId.id = :userId
            AND cr.status = 'PENDING'
            AND cr.seen = false
            """)
    long countUnreadRequests(Long userId);

    @Query("""
            SELECT cr FROM ConnectionRequest cr
            WHERE cr.targetId.id = :userId
            AND cr.status = 'PENDING'
            AND cr.seen = false
            """)
    List<ConnectionRequest> findUnreadRequests(Long userId);

}
