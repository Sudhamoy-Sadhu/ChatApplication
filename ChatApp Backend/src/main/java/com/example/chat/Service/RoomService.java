package com.example.chat.Service;

import com.example.chat.Model.Room;
import com.example.chat.Repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    // =====================
    // PRIVATE CHAT ROOM
    // =====================

    public Room getOrCreatePrivateRoom(Long userA, Long userB) {

        String uniqueKey = createUniqueKey(userA, userB);

        return roomRepository.findByUniqueKey(uniqueKey)
                .orElseGet(() -> createPrivateRoom(uniqueKey, userA));
    }

    private Room createPrivateRoom(String uniqueKey, Long createdBy) {
        Room room = Room.builder()
                .type("PRIVATE")
                .uniqueKey(uniqueKey)
                .createdBy(createdBy)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return roomRepository.save(room);
    }

    private String createUniqueKey(Long userA, Long userB) {
        Long min = Math.min(userA, userB);
        Long max = Math.max(userA, userB);

        return min + "_" + max; // stable pair key
    }

    // =====================
    // GROUP ROOM CREATION
    // =====================

    public Room createGroupRoom(String groupName, Long createdBy) {

        String uniqueKey = "group_" + System.currentTimeMillis();

        Room room = Room.builder()
                .type("GROUP")
                .uniqueKey(uniqueKey)
                .createdBy(createdBy)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return roomRepository.save(room);
    }

    // =====================
    // UPDATE ROOM METADATA
    // =====================

    public void updateLastMessage(Long roomId, Long senderId, String message) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setLastMessage(message);
        room.setLastMessageSender(senderId);
        room.setLastMessageTime(Instant.now());

        roomRepository.save(room);
    }
}
