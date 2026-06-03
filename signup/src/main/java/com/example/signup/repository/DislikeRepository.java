package com.example.signup.repository;

import java.util.Optional;

import org.antlr.v4.runtime.atn.SemanticContext.AND;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.signup.entity.Dislike;

public interface DislikeRepository extends JpaRepository<Dislike, Long> {
    Optional<Dislike> findByPostIdAndUserId(Long postId, Long userId);//SELECT * FROM dislike WHERE post_id=? AND user_id=?;

    @Modifying
    @Transactional
    @Query("DELETE FROM Dislike d WHERE d.postId = :postId AND d.userId = :userId")
    int deleteForPostAndUser(@Param("postId") Long postId, @Param("userId") Long userId);//DELETE FROM Dislike d WHERE d.postId=:postId AND d.userId=:userId // Deletes a specific dislike.

    int countByPostId(Long postId);//SELECT COUNT(*) FROM dislike WHERE post_id=?;//Counts total dislikes on a post.

    java.util.List<Dislike> findByUserId(Long userId);//Returns all dislikes made by a user.
}