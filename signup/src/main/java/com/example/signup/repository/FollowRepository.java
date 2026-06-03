package com.example.signup.repository;

import java.util.List;
import java.util.Optional;

import org.antlr.v4.runtime.atn.SemanticContext.AND;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.signup.entity.Follow;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    List<Follow> findByFollowerId(Long followerId);//SELECT * FROM follow WHERE follower_id = ?

    List<Follow> findByFollowingId(Long followingId);//Followers of User B → A, X, Y

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);//SELECT * FROM follow WHERE follower_id=? AND following_id=?;

    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);//Deletes follow relationship. //DELETE FROM follow WHERE follower_id=? AND following_id=?;

    long countByFollowingId(Long followingId);

    long countByFollowerId(Long followerId);

    // Initial load (newest 10)
    List<Follow> findTop10ByFollowingIdOrderByIdDesc(Long followingId);
    List<Follow> findTop10ByFollowerIdOrderByIdDesc(Long followerId);

    // Cursor-based paging (next 10)
    List<Follow> findTop10ByFollowingIdAndIdLessThanOrderByIdDesc(Long followingId, Long id);
    List<Follow> findTop10ByFollowerIdAndIdLessThanOrderByIdDesc(Long followerId, Long id);
}
//SELECT * FROM follow
// WHERE following_id=?
// AND id < ?
// ORDER BY id DESC
// LIMIT 10;
