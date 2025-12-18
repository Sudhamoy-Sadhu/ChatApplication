package com.example.chat.Repository;

import com.example.chat.Model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

  List<Message> findByRoomIdOrderBySentAtAsc(Long roomId);

  @Query("""
      SELECT COUNT(m)
      FROM Message m
      WHERE m.roomId = :roomId
      AND m.senderId <> :userId
      AND :userId NOT MEMBER OF m.readByUserIds
      """)
  int countUnread(Long roomId, Long userId);

  @Query("""
          SELECT m
          FROM Message m
          WHERE m.roomId = :roomId
            AND m.senderId <> :userId
            AND :userId NOT MEMBER OF m.readByUserIds
      """)
  List<Message> findUnreadMessages(Long roomId, Long userId);

  @Query("""
          SELECT m
          FROM Message m
          WHERE m.roomId = :roomId
            AND m.senderId <> :userId
            AND :userId NOT MEMBER OF m.deliveredToUserIds
      """)
  List<Message> findUndeliveredMessages(
      @Param("roomId") Long roomId,
      @Param("userId") Long userId);

  @Query("""
          SELECT m FROM Message m
          WHERE m.senderId <> :userId
            AND :userId NOT MEMBER OF m.deliveredToUserIds
      """)
  List<Message> findAllUndelivered(@Param("userId") Long userId);

  @Modifying
  @Query(value = """
        INSERT INTO message_delivered (message_id, user_id)
        SELECT m.id, :userId
        FROM message m
        WHERE m.room_id IN :roomIds
          AND m.sender_id <> :userId
          AND NOT EXISTS (
              SELECT 1 FROM message_read mr
              WHERE mr.message_id = m.id AND mr.user_id = :userId
          )
        ON CONFLICT (message_id, user_id) DO NOTHING
      """, nativeQuery = true)
  void markDeliveredSafe(
      @Param("roomIds") List<Long> roomIds,
      @Param("userId") Long userId);

  @Modifying
  @Query(value = """
        INSERT INTO message_delivered (message_id, user_id)
        VALUES (:messageId, :userId)
        ON CONFLICT DO NOTHING
      """, nativeQuery = true)
  void markDeliveredOne(
      @Param("messageId") Long messageId,
      @Param("userId") Long userId);

      
}
