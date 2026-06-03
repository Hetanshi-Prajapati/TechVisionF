package com.example.signup.repository;

import java.util.List;

import org.antlr.v4.runtime.atn.SemanticContext.AND;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.signup.entity.PostReport;

@Repository
public interface PostReportRepository extends JpaRepository<PostReport, Long> { //Item	Value , Entity	PostReport , Primary Key	Long id

    /* SELECT EXISTS(...)
    FROM post_report
    WHERE post_id = ?
    AND reporter_id = ?
    */
    boolean existsByPostIdAndReporterId(Long postId, Long reporterId);

    /*SELECT * FROM post_report
    ORDER BY created_at DESC; */
    List<PostReport> findAllByOrderByCreatedAtDesc();

    /*SELECT * FROM post_report
    WHERE status = ?
    ORDER BY created_at DESC; */
    List<PostReport> findByStatusOrderByCreatedAtDesc(PostReport.Status status);

    /*SELECT COUNT(*)
    FROM post_report
    WHERE status = ? */
    long countByStatus(PostReport.Status status);

    @Modifying(clearAutomatically = true)//Clears Hibernate cache automatically.
    @Transactional
    @Query("UPDATE PostReport r SET r.reporterUsername = :newUsername WHERE r.reporterId = :reporterId")
    void updateReporterUsername(@Param("reporterId") Long reporterId, @Param("newUsername") String newUsername);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE PostReport r SET r.postAuthor = :newUsername WHERE r.postId IN (SELECT p.id FROM Post p WHERE p.authorId = :authorId)")
    void updatePostAuthorUsername(@Param("authorId") Long authorId, @Param("newUsername") String newUsername);
}

/*
Why use EXISTS instead of COUNT(*)?
EXISTS is faster for checking existence because:
Database stops searching after finding the first match
COUNT(*) counts all matching rows
So for checking:
“Does this record exist?”
EXISTS is preferred.
*/