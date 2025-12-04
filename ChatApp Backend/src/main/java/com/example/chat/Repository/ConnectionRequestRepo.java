package com.example.chat.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.chat.Model.ConnectionRequest;

@Repository
public interface ConnectionRequestRepo extends JpaRepository<ConnectionRequest,Long> {
    Optional<ConnectionRequest> findByRequesterIdIdAndTargetIdId(Long requesterId, Long targetId);

    List<ConnectionRequest> findByTargetIdIdAndStatus(Long targetId, ConnectionRequest.Status status);
}
