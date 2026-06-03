package com.example.signup.repository;

import java.util.List;

//Used for pagination (page-wise data loading)Example:Page 1 → 10 comments,Page 2 → next 10 comments
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;//Gives built-in methods like:save(),findById(),delete(),findAll()

//Used for custom SQL / JPQL queries
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

//Used to pass values into queries safely.
import org.springframework.data.repository.query.Param;

//query runs completely OR not at all
import org.springframework.transaction.annotation.Transactional;

//This repository works with Comment table/entity
import com.example.signup.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    //Find all comments for a specific post//SELECT * FROM comments WHERE post_id = ? ORDER BY created_at DESC;
    List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId);

    //Find all comments for a specific post with pagination, returns data page by page
    Page<Comment> findByPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);

    //Find all comments by a specific user
    List<Comment> findByUserIdOrderByCreatedAtDesc(Long userId);//SELECT * FROM comments WHERE user_id = ? ORDER BY created_at DESC;

    //Count comments for a specific post////SELECT COUNT(*) FROM comments WHERE post_id = ?
    int countByPostId(Long postId);

    //Count comments by a specific user
    int countByUserId(Long userId);

    //Delete all comments for a specific post (used when post is deleted)
    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.postId = :postId")
    int deleteByPostId(@Param("postId") Long postId);

    //Delete all comments by a specific user (used when user is deleted)
    @Modifying//This query changes database data
    @Transactional//Runs query inside transaction.//If failure → rollback.
    //DELETE FROM Comment
    @Query("DELETE FROM Comment c WHERE c.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);//@Param -->:userId  ←→ method parameter
}
