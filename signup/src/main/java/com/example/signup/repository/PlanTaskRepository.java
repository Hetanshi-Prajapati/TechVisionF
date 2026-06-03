package com.example.signup.repository;

import java.util.List;

import org.antlr.v4.runtime.atn.SemanticContext.AND;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.signup.entity.PlanTask;

public interface PlanTaskRepository extends JpaRepository<PlanTask, Long> {
    List<PlanTask> findByUserId(Long userId); //SELECT * FROM plan_task WHERE user_id = ?
    List<PlanTask> findByUserIdAndWeek(Long userId, Integer week); //SELECT * FROM plan_task WHERE user_id = ? AND week = ?
    List<PlanTask> findByUserIdAndDomainAndLanguage(Long userId, String domain, String language); //SELECT * FROM plan_task WHERE user_id = ? AND domain = ? AND language = ?
}
