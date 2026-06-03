/*
PostRepository — Examiner Overview
Purpose:
- This interface defines data-access methods for the Post entity using Spring Data JPA.
- Method names are parsed by Spring to auto-generate SQL, so we avoid manual queries.

Key Concepts:
- Derived queries: findBy…OrderBy… methods translate directly into WHERE/ORDER BY SQL.
- Pagination: Page and Pageable provide page-number, size, and sorting for infinite scroll.
- Sorting strategies:
  • Home feed: newest first (createdAt DESC) for followed authors.
  • Explore feed: popularity first (likeCount DESC), then recency (createdAt DESC) for non-followed authors.

Methods Mapping:
- findByAuthorIdInOrderByCreatedAtDesc: posts from followed authors, newest first.
- findByAuthorIdNotInOrderByLikeCountDescCreatedAtDesc: posts from non-followed authors, popular first.
- Pageable variants (findByAuthorIdIn / findByAuthorIdNotIn): controller supplies sort (createdAt or likeCount+createdAt).
- Text search: findByContentContainingIgnoreCaseOrCodeContainingIgnoreCase scans content/code for keywords.

How to Explain in Viva:
1) Show how naming conventions produce SQL automatically (no @Query needed).
2) Demonstrate Home: call /api/posts/feed?page=0&size=20 → returns Page slice via Pageable.
3) Demonstrate Explore: call /api/posts/explore?page=0&size=20 → sorted by likeCount, then createdAt.
4) Mention scalability: Pageable supports infinite scroll; methods avoid complex joins by filtering on authorId sets.
*/
package com.example.signup.repository; // Declares this file belongs to the repository package for data access

import java.util.Collection; // Provides the Collection type for author ID sets
import java.util.List; // Provides the List type for returning collections of posts

import org.springframework.data.domain.Page; // Spring Data base interface with CRUD and query features
import org.springframework.data.domain.Pageable; // Represents a paginated slice of results
import org.springframework.data.jpa.repository.JpaRepository; // Encapsulates pagination and sorting information

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import com.example.signup.entity.Post; // Imports the Post JPA entity mapped to the posts table

public interface PostRepository extends JpaRepository<Post, Long> { // Repository interface for Post with primary key
                                                                    // type Long
  List<Post> findAllByDeletedFalseOrderByCreatedAtDesc();

  List<Post> findAllByDeletedFalseOrderByLikeCountDescCreatedAtDesc();

  @Query("SELECT p FROM Post p WHERE p.authorId IN :authorIds AND p.deleted = false")
  Page<Post> findByAuthorIdInAndDeletedFalse(@Param("authorIds") Collection<Long> authorIds, Pageable pageable);

  @Query("SELECT p FROM Post p WHERE p.authorId NOT IN :authorIds AND p.deleted = false")
  Page<Post> findByAuthorIdNotInAndDeletedFalse(@Param("authorIds") Collection<Long> authorIds, Pageable pageable);

  // @Query("SELECT p FROM Post p WHERE p.deleted = false OR p.deleted IS NULL")
  // Page<Post> findVisiblePosts(Pageable pageable);
  @Query("SELECT p FROM Post p WHERE p.deleted = false")
  Page<Post> findExplorePosts(Pageable pageable);

  Page<Post> findByDeletedFalse(Pageable pageable);

  Page<Post> findByDeletedFalseAndContentContainingIgnoreCase(String content, Pageable pageable);

  Page<Post> findByDeletedTrue(Pageable pageable);

  Page<Post> findByDeletedTrueAndContentContainingIgnoreCase(String content, Pageable pageable);

  Page<Post> findByContentContainingIgnoreCase(String content, Pageable pageable);

  Page<Post> findByCategoryAndDeletedFalse(String category, Pageable pageable);

  List<Post> findByAuthorIdAndDeletedFalseOrderByCreatedAtDesc(Long authorId);

  List<Post> findByIdInAndDeletedFalseOrderByCreatedAtDesc(java.util.Collection<Long> ids);

  @Query("""
      SELECT p FROM Post p
      WHERE p.deleted = false
      AND (
        LOWER(COALESCE(p.content, '')) LIKE LOWER(CONCAT('%', :term, '%'))
        OR LOWER(COALESCE(p.code, '')) LIKE LOWER(CONCAT('%', :term, '%'))
      )
      ORDER BY p.createdAt DESC
      """)
  List<Post> findByContentContainingIgnoreCaseOrCodeContainingIgnoreCaseAndDeletedFalse(@Param("term") String term);

  long countByAuthorIdAndDeletedFalse(Long authorId);

  @Modifying(clearAutomatically = true)
  @Transactional
  @Query("UPDATE Post p SET p.author = :newUsername WHERE p.authorId = :authorId")
  void updateAuthorUsername(@Param("authorId") Long authorId, @Param("newUsername") String newUsername);
}

// Hey, i am Going to share my Secret goal with you
// how to remove data from particular table? in sql workbench

// if i want to remove posts which is posted by them then entire post should be
// delete, not only content is delete and posts displaying but empts