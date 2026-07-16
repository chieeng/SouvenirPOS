package edu.cit.erag.souvenirpos.shared.security;

import edu.cit.erag.souvenirpos.shared.domain.User;
import edu.cit.erag.souvenirpos.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AppUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // AppUserDetails carries tokenVersion + enabled so the JWT filter can enforce
        // revocation and deactivation on every request.
        return new AppUserDetails(user);
    }
}
