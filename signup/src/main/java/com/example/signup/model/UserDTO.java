package com.example.signup.model;


// package com.example.signup.model;

// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.Table;

// @Entity
// @Table(name = "users")
// public class User {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @Column(nullable = false)
//     private String fullName;

//     @Column(nullable = false, unique = true)
//     private String username;

//     @Column(nullable = false, unique = true)
//     private String email;

//     private String githubUsername;

//     private String primarySkill;

//     @Column(nullable = false)
//     private String password;

//     // ===== Getters & Setters =====

//     public Long getId() {
//         return id;
//     }

//     public void setId(Long id) {
//         this.id = id;
//     }

//     public String getFullName() {
//         return fullName;
//     }

//     public void setFullName(String fullName) {
//         this.fullName = fullName;
//     }

//     public String getUsername() {
//         return username;
//     }
    
//     public void setUsername(String username) {
//         this.username = username;
//     }

//     public String getEmail() {
//         return email;
//     }
    
//     public void setEmail(String email) {
//         this.email = email;
//     }

//     public String getGithubUsername() {
//         return githubUsername;
//     }

//     public void setGithubUsername(String githubUsername) {
//         this.githubUsername = githubUsername;
//     }

//     public String getPrimarySkill() {
//         return primarySkill;
//     }

//     public void setPrimarySkill(String primarySkill) {
//         this.primarySkill = primarySkill;
//     }

//     public String getPassword() {
//         return password;
//     }
    
//     public void setPassword(String password) {
//         this.password = password;
//     }
// }



// package com.example.signup.model;

// import jakarta.persistence.*;

// @Entity
// @Table(name = "users")
// public class User {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @Column(nullable = false)
//     private String fullName;

//     @Column(nullable = false, unique = true)
//     private String username;

//     @Column(nullable = false, unique = true)
//     private String email;

//     private String githubUsername;

//     private String primarySkill;

//     @Column(nullable = false)
//     private String password;

//     // getters & setters
//     public Long getId() { return id; }
//     public void setId(Long id) { this.id = id; }

//     public String getFullName() { return fullName; }
//     public void setFullName(String fullName) { this.fullName = fullName; }

//     public String getUsername() { return username; }
//     public void setUsername(String username) { this.username = username; }

//     public String getEmail() { return email; }
//     public void setEmail(String email) { this.email = email; }

//     public String getGithubUsername() { return githubUsername; }
//     public void setGithubUsername(String githubUsername) { this.githubUsername = githubUsername; }

//     public String getPrimarySkill() { return primarySkill; }
//     public void setPrimarySkill(String primarySkill) { this.primarySkill = primarySkill; }

//     public String getPassword() { return password; }
//     public void setPassword(String password) { this.password = password; }
// }




// package com.example.signup.model;//Tells Java that this file belongs to the model package.Model = represents database tables.

// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.Table;

//These are JPA (Hibernate) annotations used to connect Java class ↔ Database table.

// Annotation	Purpose
// @Entity	Marks class as database entity
// @Table	Sets table name
// @Id	Primary key
// @GeneratedValue	Auto-generate ID
// @Column	Column rules (unique, nullable, etc.)

// @Entity//“This class represents a table in the database.”
// @Table(name = "users")//Database table name will be users.If not written, table name would be user.
// public class User {//Java class named User.

//     @Id//Marks this field as Primary Key.
//     @GeneratedValue(strategy = GenerationType.IDENTITY)//Database will auto-generate ID.Example: 1, 2, 3, 4 …
//     private Long id;//column: id ,type long //id BIGINT AUTO_INCREMENT PRIMARY KEY

//     private String name;//column: name ,stores full name of user

//     @Column(unique = true)//No two users can have same username
//     private String username;

//     @Column(unique = true)//Email must be unique in database.
//     private String email;

//     private String password;//Stores user password.Currently plain text (not safe — will hash later).

//     private String githubUsername;//Stores GitHub username of developer.

//     private String primarySkill;//Stores main skill (Java, Python, React, etc.).

//     // getters & setters //Used to access and modify private variables.
//     public Long getId() { return id; }
//     public void setId(Long id) { this.id = id; }

//     public String getName() { return name; }
//     public void setName(String name) { this.name = name; }

//     public String getUsername() { return username; }
//     public void setUsername(String username) { this.username = username; }

//     public String getEmail() { return email; }
//     public void setEmail(String email) { this.email = email; }

//     public String getPassword() { return password; }
//     public void setPassword(String password) { this.password = password; }

//     public String getGithubUsername() { return githubUsername; }
//     public void setGithubUsername(String githubUsername) {
//         this.githubUsername = githubUsername;
//     }

//     public String getPrimarySkill() { return primarySkill; }
//     public void setPrimarySkill(String primarySkill) {
//         this.primarySkill = primarySkill;
//     }
// }




public class UserDTO {

    private String fullName;
    private String username;
    private String email;
    private String password;
    private String githubUsername;
    private String primarySkill;

    // Getters and Setters

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    public String getGithubUsername() {
        return githubUsername;
    }

    public void setGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
    }

    public String getPrimarySkill() {
        return primarySkill;
    }

    public void setPrimarySkill(String primarySkill) {
        this.primarySkill = primarySkill;
    }
}