package com.example.chat.Repository;

import java.util.List;
import java.util.Optional;

import org.antlr.v4.runtime.atn.SemanticContext.OR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.chat.Model.User;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    boolean existsByUsernameOrEmail(String username, String email);

    @Query("SELECT u FROM User u WHERE (LOWER(u.username) LIKE LOWER (CONCAT('%', :query, '%'))OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))) AND u.id <> :excludeId")
    List<User> searchUsersExcludeLoggedIn(
            @Param("query") String query,
            @Param("excludeId") Long excludeId);

    Optional<User> findByEmail(String email);

    Optional<User> findEmailById(Long id);

}
