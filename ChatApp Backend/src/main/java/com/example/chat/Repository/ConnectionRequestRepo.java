package com.example.chat.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Modifying
    @Query("""
                DELETE FROM ConnectionRequest cr
                WHERE cr.status = 'REJECTED'
                AND cr.updatedAt < :cutoff
            """)
    int deleteOldRejectedRequests(@Param("cutoff") Instant cutoff);

    @Query("""
                   SELECT cr FROM ConnectionRequest cr
                   WHERE (cr.requesterId.id = :user1 AND cr.targetId.id = :user2)
                   OR (cr.requesterId.id = :user2 AND cr.targetId.id = :user1)
            """)
    Optional<ConnectionRequest> findBetweenUsers(
            @Param("user1") Long user1,
            @Param("user2") Long user2);

}
