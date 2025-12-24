package com.example.chat.Repository;

import com.example.chat.Model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

  List<Message> findByRoomIdOrderBySentAtAsc(Long roomId);

  // ✅ NEW: Fetch message with collections eagerly to avoid N+1 or Lazy issues in
  // Listener
  @Query("SELECT m FROM Message m " +
      "LEFT JOIN FETCH m.readByUserIds " +
      "LEFT JOIN FETCH m.deliveredToUserIds " +
      "WHERE m.id = :id")
  Optional<Message> findWithReceiptsById(@Param("id") Long id);

  // ✅ Only fetch unread message IDs
  @Query("""
      SELECT m.id
      FROM Message m
      WHERE m.roomId = :roomId
        AND m.senderId <> :userId
        AND :userId NOT MEMBER OF m.readByUserIds
      """)
  List<Long> findUnreadIds(@Param("roomId") Long roomId, @Param("userId") Long userId);

  // ✅ Only fetch id + senderId for notifications
  @Query("SELECT m.id, m.senderId FROM Message m WHERE m.id IN :ids")
  List<Object[]> findIdsAndSenderForIds(@Param("ids") List<Long> ids);

  // ✅ Native insert: mark messages as read
  @Modifying
  @Query(value = """
      INSERT INTO message_read (message_id, user_id)
      SELECT m.id, :userId
      FROM messages m
      WHERE m.room_id = :roomId
        AND m.sender_id <> :userId
        AND NOT EXISTS (
            SELECT 1 FROM message_read mr
            WHERE mr.message_id = m.id AND mr.user_id = :userId
        )
      ON CONFLICT DO NOTHING
      """, nativeQuery = true)
  void markRoomAsRead(@Param("roomId") Long roomId, @Param("userId") Long userId);

  // ✅ Native insert: mark all undelivered messages for this user
  @Modifying
  @Query(value = """
      INSERT INTO message_delivered (message_id, user_id)
      SELECT m.id, :userId
      FROM messages m
      WHERE m.sender_id <> :userId
        AND NOT EXISTS (
            SELECT 1 FROM message_delivered md
            WHERE md.message_id = m.id AND md.user_id = :userId
        )
      ON CONFLICT DO NOTHING
      """, nativeQuery = true)
  void markAllAsDeliveredForUser(@Param("userId") Long userId);

  // ✅ Single message insert delivered
  @Modifying
  @Query(value = """
      INSERT INTO message_delivered (message_id, user_id)
      VALUES (:messageId, :userId)
      ON CONFLICT DO NOTHING
      """, nativeQuery = true)
  void markDeliveredOne(@Param("messageId") Long messageId, @Param("userId") Long userId);

  // ✅ Single message insert read
  @Modifying
  @Query(value = """
      INSERT INTO message_read (message_id, user_id)
      VALUES (:messageId, :userId)
      ON CONFLICT DO NOTHING
      """, nativeQuery = true)
  void markReadOne(@Param("messageId") Long messageId, @Param("userId") Long userId);

  // ✅ Count unread messages
  @Query(value = """
      SELECT COUNT(*) FROM messages m
      WHERE m.room_id = :roomId
        AND m.sender_id <> :userId
        AND NOT EXISTS (
            SELECT 1 FROM message_read mr
            WHERE mr.message_id = m.id AND mr.user_id = :userId
        )
      """, nativeQuery = true)
  int countUnread(@Param("roomId") Long roomId, @Param("userId") Long userId);

  @Query(value = """
      SELECT m.* FROM messages m
      WHERE m.sender_id <> :userId
        AND NOT EXISTS (
            SELECT 1 FROM message_delivered md
            WHERE md.message_id = m.id AND md.user_id = :userId
        )
      """, nativeQuery = true)
  List<Message> findAllUndelivered(@Param("userId") Long userId);

  @Query("SELECT DISTINCT m FROM Message m " +
      "LEFT JOIN FETCH m.readByUserIds " +
      "LEFT JOIN FETCH m.deliveredToUserIds " +
      "WHERE m.roomId = :roomId ORDER BY m.sentAt ASC")
  List<Message> findByRoomIdWithReceipts(@Param("roomId") Long roomId);
}