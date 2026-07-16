package edu.cit.erag.souvenirpos.user.service;

import edu.cit.erag.souvenirpos.user.dto.UserCreateRequest;
import edu.cit.erag.souvenirpos.shared.domain.Role;
import edu.cit.erag.souvenirpos.shared.domain.User;
import edu.cit.erag.souvenirpos.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User(
                request.getName(),
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole()
        );
        // The owner sets a temporary password; the staff member must replace it on first login.
        user.setMustChangePassword(true);
        return userRepository.save(user);
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    /**
     * Self-service password change. Verifies the current password, stores the new hash,
     * clears the must-change flag, and bumps the token version so every previously issued
     * token for this user (including the one that made this call) is invalidated.
     *
     * @return the user with its new token version, so the caller can mint a fresh token.
     */
    @Transactional
    public User changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("New password must be different from the current one");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        user.incrementTokenVersion();
        return userRepository.save(user);
    }

    /**
     * Owner action: disable an account. Kept (not deleted) so its past sales stay attributed
     * for the audit trail; the token version bump revokes any active session immediately.
     */
    @Transactional
    public User setEnabled(Long userId, boolean enabled, String actingUsername) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (!enabled && target.getUsername().equals(actingUsername)) {
            throw new IllegalArgumentException("You cannot deactivate your own account");
        }

        // Defense-in-depth: never let the store be left with zero active owners. The target is
        // still enabled here, so a count of 1 means it is the last one.
        if (!enabled && target.getRole() == Role.OWNER
                && userRepository.countByRoleAndEnabledTrue(Role.OWNER) <= 1) {
            throw new IllegalArgumentException("Cannot deactivate the last active owner");
        }

        target.setEnabled(enabled);
        if (!enabled) {
            target.incrementTokenVersion(); // force-logout the deactivated user
        }
        return userRepository.save(target);
    }

    /** Owner action: force-logout a user everywhere without disabling the account. */
    @Transactional
    public User forceLogout(Long userId) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        target.incrementTokenVersion();
        return userRepository.save(target);
    }
}
