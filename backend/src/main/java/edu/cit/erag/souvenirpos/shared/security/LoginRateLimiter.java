package edu.cit.erag.souvenirpos.shared.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Throttles failed login attempts to blunt password brute-forcing. A fixed window is
 * tracked per key (username + client IP); once {@code maxAttempts} failures accumulate
 * inside {@code windowSeconds}, further attempts are blocked until the window rolls over.
 * A successful login clears the counter for that key.
 *
 * <p>State is in-memory, so it protects a single instance and resets on restart. That is
 * sufficient for the single-node deployment this app targets; a multi-instance deployment
 * should back this with a shared store (e.g. Redis / Bucket4j).
 */
@Component
public class LoginRateLimiter {

    private final int maxAttempts;
    private final Duration window;
    private final Map<String, Attempts> attemptsByKey = new ConcurrentHashMap<>();

    public LoginRateLimiter(
            @Value("${souvenirpos.login.max-attempts:5}") int maxAttempts,
            @Value("${souvenirpos.login.window-seconds:300}") long windowSeconds) {
        this.maxAttempts = maxAttempts;
        this.window = Duration.ofSeconds(windowSeconds);
    }

    /** @return true if this key is currently locked out and the attempt must be refused. */
    public boolean isBlocked(String key) {
        Attempts attempts = attemptsByKey.get(normalize(key));
        return attempts != null && attempts.isBlocked(maxAttempts, window);
    }

    public void recordFailure(String key) {
        attemptsByKey.compute(normalize(key), (k, existing) -> {
            Instant now = Instant.now();
            if (existing == null || existing.isExpired(window, now)) {
                return new Attempts(1, now);
            }
            return new Attempts(existing.count + 1, existing.windowStart);
        });
    }

    public void recordSuccess(String key) {
        attemptsByKey.remove(normalize(key));
    }

    private static String normalize(String key) {
        return key == null ? "" : key.toLowerCase();
    }

    private static final class Attempts {
        private final int count;
        private final Instant windowStart;

        private Attempts(int count, Instant windowStart) {
            this.count = count;
            this.windowStart = windowStart;
        }

        private boolean isExpired(Duration window, Instant now) {
            return windowStart.plus(window).isBefore(now);
        }

        private boolean isBlocked(int maxAttempts, Duration window) {
            return count >= maxAttempts && !isExpired(window, Instant.now());
        }
    }
}
