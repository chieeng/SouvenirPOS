package edu.cit.erag.souvenirpos.service;

import edu.cit.erag.souvenirpos.dto.UserCreateRequest;
import edu.cit.erag.souvenirpos.entity.User;
import edu.cit.erag.souvenirpos.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        return userRepository.save(user);
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }
}
