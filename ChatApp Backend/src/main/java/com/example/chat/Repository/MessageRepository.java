package com.example.chat.Repository;

import com.example.chat.Model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
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
          SELECT m FROM Message m
          WHERE m.senderId <> :userId
            AND :userId NOT MEMBER OF m.deliveredToUserIds
      """)
  List<Message> findAllUndelivered(@Param("userId") Long userId);

}
