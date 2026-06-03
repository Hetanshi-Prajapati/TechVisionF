package com.example.signup.entity;
//These annotations connect Java with database.
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity//represents a database table.
@Table(name = "app_settings")
public class AppSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//Database auto-generates value.,You DO NOT manually assign id.
    private Long id;

    @Column(nullable = false, unique = true)
    private String settingKey;

    @Column(nullable = false)
    private String settingValue;

    public AppSettings() {}

    public AppSettings(String settingKey, String settingValue) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }
}
//It allows your app to have dynamic configuration.
//Sets mode of the application: Production or Test Mode.