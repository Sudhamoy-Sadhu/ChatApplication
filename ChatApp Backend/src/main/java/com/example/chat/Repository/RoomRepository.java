package com.example.chat.Repository;

import com.example.chat.Model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByUniqueKey(String uniqueKey);

    boolean existsByUniqueKey(String uniqueKey);

    @Query("""
                SELECT r.id
                FROM Room r
                WHERE
                    (r.type = 'PRIVATE' AND r.uniqueKey LIKE %:userId%)
                    OR r.type = 'GROUP'
            """)
    List<Long> findRoomIdsForUser(Long userId);
}
