package com.example.chat.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.chat.Model.RoomParticipant;

public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {

    @Query("SELECT rp.user.id FROM RoomParticipant rp WHERE rp.room.id = :roomId")
    List<Long> findUserIdsByRoomId(Long roomId);
}

