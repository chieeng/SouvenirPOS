package edu.cit.erag.souvenirpos.auth.controller;

import edu.cit.erag.souvenirpos.auth.dto.LoginRequest;
import edu.cit.erag.souvenirpos.auth.dto.LoginResponse;
import edu.cit.erag.souvenirpos.shared.domain.User;
import edu.cit.erag.souvenirpos.shared.exception.TooManyLoginAttemptsException;
import edu.cit.erag.souvenirpos.shared.security.LoginRateLimiter;
import edu.cit.erag.souvenirpos.user.repository.UserRepository;
import edu.cit.erag.souvenirpos.shared.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final LoginRateLimiter loginRateLimiter;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          JwtService jwtService,
                          LoginRateLimiter loginRateLimiter) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.loginRateLimiter = loginRateLimiter;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        // Rate-limit by username + client IP so repeated failures against one account
        // (or from one host) are throttled before the credentials are ever checked.
        String rateKey = request.getUsername() + "|" + clientIp(httpRequest);
        if (loginRateLimiter.isBlocked(rateKey)) {
            throw new TooManyLoginAttemptsException("Too many login attempts. Please wait and try again.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (Exception ex) {
            loginRateLimiter.recordFailure(rateKey);
            throw new BadCredentialsException("Invalid username or password");
        }

        loginRateLimiter.recordSuccess(rateKey);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());

        return new LoginResponse(token, user.getId(), user.getName(), user.getUsername(), user.getRole().name());
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // First hop is the original client when set by a trusted reverse proxy.
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
