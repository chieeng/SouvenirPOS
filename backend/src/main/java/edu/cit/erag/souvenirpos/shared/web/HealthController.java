package edu.cit.erag.souvenirpos.shared.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Public, unauthenticated health/landing endpoint. Gives the deployment root a friendly 200
 * response (instead of the API's default 401) and a stable path for platform health checks.
 */
@RestController
public class HealthController {

    @GetMapping({"/", "/health"})
    public Map<String, String> health() {
        return Map.of("status", "ok", "service", "SouvenirPOS API");
    }
}
