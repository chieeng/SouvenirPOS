package edu.cit.erag.souvenirpos.shared.config;

import edu.cit.erag.souvenirpos.shared.domain.Category;
import edu.cit.erag.souvenirpos.shared.domain.Role;
import edu.cit.erag.souvenirpos.shared.domain.User;
import edu.cit.erag.souvenirpos.repository.CategoryRepository;
import edu.cit.erag.souvenirpos.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "Bracelets", "RTW", "T-Shirt", "Assorted", "Bag", "Drinks", "Hat",
            "Lanyard", "Payong", "Ref Magnet", "Rosary", "Sarong", "Shades",
            "Toys", "Tsinelas", "Tubig");

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final String seedUsername;
    private final String seedPassword;

    public DataSeeder(
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            PasswordEncoder passwordEncoder,
            @Value("${souvenirpos.seed.owner-username}") String seedUsername,
            @Value("${souvenirpos.seed.owner-password}") String seedPassword) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
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

        if (categoryRepository.count() == 0) {
            DEFAULT_CATEGORIES.forEach(name -> categoryRepository.save(new Category(name)));
            log.info("Seeded {} default souvenir categories.", DEFAULT_CATEGORIES.size());
        }
    }
}
