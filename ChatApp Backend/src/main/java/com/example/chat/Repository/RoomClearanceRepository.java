package com.example.chat.Repository;

import com.example.chat.Model.RoomClearance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoomClearanceRepository extends JpaRepository<RoomClearance, Long> {
    Optional<RoomClearance> findByRoomIdAndUserId(Long roomId, Long userId);
}
