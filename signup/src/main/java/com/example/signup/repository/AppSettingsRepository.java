package com.example.signup.repository;

import com.example.signup.entity.AppSettings;//connecting this repository to AppSettings table.
import org.springframework.data.jpa.repository.JpaRepository;//JpaRepository gives you ready-made database functions
import java.util.Optional;

public interface AppSettingsRepository extends JpaRepository<AppSettings, Long> { //This repository manages app_settings table where ID is Long.
    Optional<AppSettings> findBySettingKey(String settingKey); //It searches the database for an AppSettings record where settingKey matches the given value, and returns it safely (or empty if not found) using Optional.
}
