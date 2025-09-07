package com.example.chat.Service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory room membership service.
 * Replace with DB-backed service later.
 */
@Service
public class ChatApplicationService {

    // roomId -> set of usernames
    private final Map<String, Set<String>> roomMembers = new ConcurrentHashMap<>();

    public void addMember(String roomId, String username) {
        roomMembers.computeIfAbsent(roomId, id -> ConcurrentHashMap.newKeySet()).add(username);
    }

    public void removeMember(String roomId, String username) {
        Set<String> members = roomMembers.get(roomId);
        if (members != null) {
            members.remove(username);
            if (members.isEmpty()) {
                // optional: remove empty room from map
                roomMembers.remove(roomId);
            }
        }
    }

    public boolean isMember(String roomId, String username) {
        Set<String> members = roomMembers.get(roomId);
        return members != null && members.contains(username);
    }

    public Set<String> getMembers(String roomId) {
        return roomMembers.getOrDefault(roomId, Set.of());
    }
}
