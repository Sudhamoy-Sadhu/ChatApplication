package com.example.chat.Repository;

import com.example.chat.Model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByUniqueKey(String uniqueKey);

    boolean existsByUniqueKey(String uniqueKey);
}
