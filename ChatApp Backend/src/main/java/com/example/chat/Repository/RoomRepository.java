package com.example.chat.Repository;

import com.example.chat.Model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByUniqueKey(String uniqueKey);

    boolean existsByUniqueKey(String uniqueKey);

    @Query("""
                SELECT rp.room.id
                FROM RoomParticipant rp
                WHERE rp.user.id = :userId
            """)
    List<Long> findRoomIdsForUser(@Param("userId") Long userId);

    @Query("SELECT rp.user.id FROM RoomParticipant rp WHERE rp.room.id = :roomId")
    List<Long> findUserIdsByRoomId(@Param("roomId") Long roomId);
}
