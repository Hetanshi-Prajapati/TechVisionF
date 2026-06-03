package com.example.signup;//Tells Java this class belongs to the base package of your project.

import java.util.Optional;//Wrapper object representing value may exist or not, Avoids NullPointerException

import org.springframework.boot.CommandLineRunner;//Interface that runs code after Spring Boot starts.
import org.springframework.boot.SpringApplication;//Starts the Spring Boot application.
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;//Create and manage this object.
 
import org.springframework.jdbc.core.JdbcTemplate;//Helper class to run SQL queries.

import com.example.signup.repository.PostRepository;//Your Post repository interface, used to access post data in the database.
import com.example.signup.entity.User;//Your User entity class.
import com.example.signup.repository.UserRepository;//Your User repository interface, used to access user data in the database.

@SpringBootApplication//Main class of your Spring Boot project.
public class SignupApplication {

	public static void main(String[] args) {
		SpringApplication.run(SignupApplication.class, args);//Java JVM starts↓Spring Boot context created↓Beans initialized↓Dependencies injected↓Server started (Tomcat)↓Application LIVE
	}

	@Bean
	CommandLineRunner seedAdmin(UserRepository userRepository, PostRepository postRepository,//Spring creates a startup task.Runs automatically AFTER startup.
			JdbcTemplate jdbcTemplate) {
		return args -> {
			// Seed Admin
			Optional<User> byUsername = userRepository.findByUsername("admin");
			Optional<User> byEmail = userRepository.findByEmail("admin@gmail.com");
			User u = byUsername.orElse(byEmail.orElse(null));
			if (u == null) {
				u = new User();
				u.setUsername("admin");
			}
			u.setFullName("Administrator");
			u.setEmail("admin@gmail.com");
			u.setPassword("Admin@123");
			u.setAdmin(true);
			userRepository.save(u);

			// Fix database state using native SQL (more reliable for NULLs)
			try {
				int rows = jdbcTemplate.update("UPDATE posts SET deleted = 0 WHERE deleted IS NULL");
				if (rows > 0)
					System.out.println("Fixed " + rows + " posts with NULL deleted status.");

				rows = jdbcTemplate.update("UPDATE posts SET like_count = 0 WHERE like_count IS NULL");
				if (rows > 0)
					System.out.println("Fixed " + rows + " posts with NULL like_count.");

				rows = jdbcTemplate.update("UPDATE posts SET dislikes = 0 WHERE dislikes IS NULL");
				if (rows > 0)
					System.out.println("Fixed " + rows + " posts with NULL dislikes.");

				rows = jdbcTemplate.update("UPDATE posts SET dislike_count = 0 WHERE dislike_count IS NULL");
				if (rows > 0)
					System.out.println("Fixed " + rows + " posts with NULL dislike_count.");

				rows = jdbcTemplate.update("UPDATE posts SET comment_count = 0 WHERE comment_count IS NULL");
				if (rows > 0)
					System.out.println("Fixed " + rows + " posts with NULL comment_count.");
			} catch (Exception e) {
				System.err.println("Database fix error: " + e.getMessage());
			}
		};
	}
}

// http://localhost:8080/api/auth/signup.html

/*
 * main()
 * ↓
 * SpringApplication.run()
 * ↓
 * Spring scans packages
 * ↓
 * Controllers, Repositories, Entities loaded
 * ↓
 * Tomcat server starts
 * ↓
 * Your APIs are ready
 * 
 */
