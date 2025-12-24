package com.example.chat.Service;

import com.example.chat.Model.Room;
import com.example.chat.Model.RoomParticipant;
import com.example.chat.Model.User;
import com.example.chat.Presence.UserSessionRegistry;
import com.example.chat.Repository.RoomParticipantRepository;
import com.example.chat.Repository.RoomRepository;
import com.example.chat.Repository.UserRepo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserSessionRegistry sessionRegistry;

    private final RoomParticipantRepository participantRepository;
    private final UserRepo userRepository;
    // =====================
    // PRIVATE CHAT ROOM
    // =====================

    @Transactional
    public Room getOrCreatePrivateRoom(Long userA, Long userB) {
        String uniqueKey = createUniqueKey(userA, userB);

        // 1. Get or Create the Room
        Room room = roomRepository.findByUniqueKey(uniqueKey)
                .orElseGet(() -> {
                    Room newRoom = Room.builder()
                            .type("PRIVATE")
                            .uniqueKey(uniqueKey)
                            .createdBy(userA)
                            .build();
                    return roomRepository.save(newRoom);
                });

        // 2. SELF-HEALING: Ensure both users are in the participants table
        List<Long> existingParticipants = participantRepository.findUserIdsByRoomId(room.getId());
        if (existingParticipants.size() < 2) {
            syncParticipants(room, userA, userB, existingParticipants);
        }

        return room;
    }

    private void syncParticipants(Room room, Long userA, Long userB, List<Long> existing) {
        List.of(userA, userB).forEach(userId -> {
            if (!existing.contains(userId)) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found: " + userId));

                RoomParticipant participant = RoomParticipant.builder()
                        .room(room)
                        .user(user)
                        .joinedAt(Instant.now())
                        .build();
                participantRepository.save(participant);
            }
        });
    }

    @Transactional
    private Room createPrivateRoom(String uniqueKey, Long createdBy, Long otherUser) {
        // 1. Create and Save the Room
        Room room = Room.builder()
                .type("PRIVATE")
                .uniqueKey(uniqueKey)
                .createdBy(createdBy)
                .build();
        Room savedRoom = roomRepository.save(room);

        // 2. Link Participants (CRITICAL STEP)
        // Assuming uniqueKey is "userA_userB", you need to add both
        List.of(createdBy, otherUser).forEach(userId -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            RoomParticipant participant = RoomParticipant.builder()
                    .room(savedRoom)
                    .user(user)
                    .joinedAt(Instant.now())
                    .build();
            participantRepository.save(participant);
        });

        return savedRoom;
    }

    public Long getRecipientId(Long roomId, Long senderId) {
        List<Long> participants = getRoomParticipants(roomId);
        return participants.stream()
                .filter(id -> !id.equals(senderId))
                .findFirst()
                .orElse(null);
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
                .build();

        return roomRepository.save(room);
    }

    // =====================
    // UPDATE ROOM METADATA
    // =====================

    @Transactional
    public void updateLastMessage(Long roomId, Long senderId, String preview, Instant sentAt) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setLastMessage(preview);
        room.setLastMessageSender(senderId);
        room.setLastMessageTime(sentAt);
        room.setUpdatedAt(sentAt);

        roomRepository.save(room);
    }

    public List<Long> getRoomParticipants(Long roomId) {
        return participantRepository.findUserIdsByRoomId(roomId);
    }

    public boolean isUserOnline(Long userId) {
        return sessionRegistry.isOnline(userId);
    }

    public List<Long> getRoomsForUser(Long userId) {
        return roomRepository.findRoomIdsForUser(userId);
    }

}
