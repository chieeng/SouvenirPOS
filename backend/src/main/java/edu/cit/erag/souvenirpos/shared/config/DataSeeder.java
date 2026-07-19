package edu.cit.erag.souvenirpos.shared.config;

import edu.cit.erag.souvenirpos.shared.domain.Role;
import edu.cit.erag.souvenirpos.shared.domain.User;
import edu.cit.erag.souvenirpos.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean adminBootstrapEnabled;
    private final String seedUsername;
    private final String seedPassword;

    public DataSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${souvenirpos.admin.bootstrap-enabled:true}") boolean adminBootstrapEnabled,
            @Value("${souvenirpos.seed.owner-username}") String seedUsername,
            @Value("${souvenirpos.seed.owner-password:}") String seedPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminBootstrapEnabled = adminBootstrapEnabled;
        this.seedUsername = seedUsername;
        this.seedPassword = seedPassword;
    }

    @Override
    public void run(String... args) {
        // Categories are deliberately NOT seeded: the owner creates them through the
        // Categories screen (FR-005), so a fresh deployment starts with an empty catalogue
        // rather than someone else's shop's default list.
        bootstrapOwnerAccount();
    }

    /**
     * One-time bootstrap of the initial owner (admin) account. Gated behind
     * {@code souvenirpos.admin.bootstrap-enabled} (env {@code ADMIN_BOOTSTRAP_ENABLED}) so it
     * can be turned off in production after the first deploy, and idempotent via an explicit
     * username check so it can never create a duplicate even while enabled.
     */
    private void bootstrapOwnerAccount() {
        if (!adminBootstrapEnabled) {
            log.info("Admin bootstrap disabled (souvenirpos.admin.bootstrap-enabled=false); "
                    + "skipping owner-account seeding.");
            return;
        }
        if (userRepository.existsByUsername(seedUsername)) {
            log.info("Owner account '{}' already exists; bootstrap skipped (no duplicate created). "
                    + "Set ADMIN_BOOTSTRAP_ENABLED=false to disable this check after deployment.",
                    seedUsername);
            return;
        }

        // If no seed password was configured, generate a strong random one so no predictable
        // default credential ever ships. A configured password is assumed to be already known
        // to the operator and is NEVER echoed to the logs.
        boolean generated = seedPassword == null || seedPassword.isBlank();
        String password = generated ? randomPassword() : seedPassword;

        // Store only the hash, using the project's configured PasswordEncoder (BCrypt).
        User owner = new User("Shop Owner", seedUsername, passwordEncoder.encode(password), Role.OWNER);
        // Force the initial credential to be replaced on first login (checklist item 7).
        owner.setMustChangePassword(true);
        userRepository.save(owner);

        if (generated) {
            // A randomly generated secret must be surfaced exactly once so the operator can
            // log in; it is not persisted in plaintext anywhere else. Rotate it after first login.
            log.warn("Seeded initial owner account -> username: '{}'. "
                    + "GENERATED one-time password: '{}'. "
                    + "Log in, create your own owner account, then change or disable this one.",
                    seedUsername, password);
        } else {
            log.info("Seeded initial owner account -> username: '{}' (password taken from configuration; "
                    + "not logged). Log in and rotate it after first use.", seedUsername);
        }
    }

    private static String randomPassword() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
