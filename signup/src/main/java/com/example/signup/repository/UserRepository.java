package com.example.signup.repository;//Tells Java this file belongs to the repository package. Repository = talks to database.

import java.util.Optional;//Imports Spring Data JPAâ€™s main interface.//â€œOptional is used to safely handle null values when data may or may not exist.

import org.springframework.data.jpa.repository.JpaRepository;//Tells repository which table/entity it works with.
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.signup.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // UserRepository â†’ your custom repository
    // JpaRepository<User, Long>:
    // User â†’ Entity (table)
    // Long â†’ Primary key type (id)
    // UserRepository â†” User Entity â†” users table

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);// Find a user whose email matches this email//SELECT * FROM users WHERE
                                             // email = ?;

    Optional<User> findByUsername(String username);// This is used when user logs in using username instead of
                                                   // email.//SELECT * FROM users WHERE username = ?;

    Optional<User> findByUsernameOrEmail(String username, String email);

    java.util.List<User> findByUsernameContainingIgnoreCase(String username);

    java.util.List<User> findByFullNameContainingIgnoreCase(String fullName);

    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    Optional<User> findByRememberToken(String rememberToken);

    Optional<User> findByResetToken(String resetToken);
}

// What Spring does automatically:
// Reads method name
// Converts it into SQL
// ðŸ“Œ Generated SQL (conceptually):
// SELECT COUNT(*) > 0 FROM users WHERE email = ?
// âž¡ï¸ Returns:
// true â†’ email already exists
// false â†’ email is new
// Used in:
// userRepository.existsByEmail(email)
// 6ï¸âƒ£ Check if username exists
// boolean existsByUsername(String username);
// ðŸ“Œ Auto-generated SQL:
// SELECT COUNT(*) > 0 FROM users WHERE username = ?
// âž¡ï¸ Prevents duplicate usernames.
// Used in:
// userRepository.existsByUsername(username)
