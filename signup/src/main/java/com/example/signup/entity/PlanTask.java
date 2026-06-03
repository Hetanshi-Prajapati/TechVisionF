package com.example.signup.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table(name = "plan_tasks")
public class PlanTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userId;
    
    // Existing fields
    @Column(name = "plan_year")
    private Integer planYear; // Changed to Integer so it can be nullable for new records
    private String task;
    private boolean completed;

    // New fields for Guided Learning Roadmap
    private String domain;
    private String language;
    private Integer week;
    private String title;
    
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String description;
    
    private String taskType; // TASK, QUIZ, PROJECT
    
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String proofUrl;
    
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String quizQuestion;
    
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String optionA;
    
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String optionB;
    
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String optionC;
    
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String optionD;
    
    private String correctAnswer;
    
    private Integer quizScore;
    
    private java.time.LocalDateTime createdAt;

    @jakarta.persistence.PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getYear() { return planYear; }
    public void setYear(Integer year) { this.planYear = year; }

    public String getTask() { return task; }
    public void setTask(String task) { this.task = task; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Integer getWeek() { return week; }
    public void setWeek(Integer week) { this.week = week; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }

    public String getProofUrl() { return proofUrl; }
    public void setProofUrl(String proofUrl) { this.proofUrl = proofUrl; }

    public String getQuizQuestion() { return quizQuestion; }
    public void setQuizQuestion(String quizQuestion) { this.quizQuestion = quizQuestion; }

    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }

    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }

    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }

    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public Integer getQuizScore() { return quizScore; }
    public void setQuizScore(Integer quizScore) { this.quizScore = quizScore; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
}
