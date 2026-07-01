package edu.cit.erag.souvenirpos.config;

import edu.cit.erag.souvenirpos.entity.Role;
import edu.cit.erag.souvenirpos.entity.User;
import edu.cit.erag.souvenirpos.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String seedUsername;
    private final String seedPassword;

    public DataSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${souvenirpos.seed.owner-username}") String seedUsername,
            @Value("${souvenirpos.seed.owner-password}") String seedPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedUsername = seedUsername;
        this.seedPassword = seedPassword;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User owner = new User("Shop Owner", seedUsername, passwordEncoder.encode(seedPassword), Role.OWNER);
            userRepository.save(owner);
            log.info("Seeded initial owner account -> username: '{}', password: '{}'. Log in and create additional accounts, then change this password.", seedUsername, seedPassword);
        }
    }
}
