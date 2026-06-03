package com.example.signup.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.signup.entity.YearGoal;

public interface YearGoalRepository extends JpaRepository<YearGoal, Long> {
    List<YearGoal> findByPlanIdOrderByYearNumberAsc(Long planId);

    YearGoal findByPlanIdAndYearNumber(Long planId, int yearNumber);

    void deleteByPlanId(Long planId);
}