package com.example.signup.repository;

import java.util.Optional;

import org.antlr.v4.runtime.atn.SemanticContext.AND;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.signup.entity.Like;

//Item	Value ,Entity	Like , Primary Key	Long id
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPostIdAndUserId(Long postId, Long userId); // SELECT * FROM likes WHERE post_id = ? AND user_id = ?

    @Modifying //This query changes data
    @Transactional //If failure → rollback.
    @Query("DELETE FROM Like l WHERE l.postId = :postId AND l.userId = :userId")// @Query DELETE FROM Like
    int deleteForPostAndUser(@Param("postId") Long postId, @Param("userId") Long userId); //@Param :postId → method parameter

    int countByPostId(Long postId); //SELECT COUNT(*) FROM likes WHERE post_id = ?
    java.util.List<Like> findByUserId(Long userId); //SELECT * FROM likes WHERE user_id = ?
}
