package com.example.signup.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.signup.entity.FiveYearPlan;

public interface FiveYearPlanRepository extends JpaRepository<FiveYearPlan, Long> {
    Optional<FiveYearPlan> findByUserId(Long userId);//SELECT * FROM five_year_plan WHERE user_id = ?;

    List<FiveYearPlan> findAllByUserId(Long userId);//Returns all plans created by a user.//SELECT * FROM five_year_plan WHERE user_id = ?;

    Optional<FiveYearPlan> findByUserIdAndDomainAndLanguageAndLevel(
            Long userId, String domain, String language, String level);

    /*SELECT * FROM five_year_plan
    WHERE user_id = ?
    AND domain = ?
    AND language = ?
    AND level = ?; */
}