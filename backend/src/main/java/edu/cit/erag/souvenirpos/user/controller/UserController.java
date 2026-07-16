package edu.cit.erag.souvenirpos.user.controller;

import edu.cit.erag.souvenirpos.user.dto.UserCreateRequest;
import edu.cit.erag.souvenirpos.user.dto.UserResponse;
import edu.cit.erag.souvenirpos.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('OWNER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody UserCreateRequest request) {
        return new UserResponse(userService.createUser(request));
    }

    @PreAuthorize("hasRole('OWNER')")
    @GetMapping
    public List<UserResponse> listUsers() {
        return userService.listUsers().stream().map(UserResponse::new).toList();
    }

    // Deactivate an account (kept for audit history) and revoke its active session.
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/{id}/deactivate")
    public UserResponse deactivate(@PathVariable Long id, Authentication authentication) {
        return new UserResponse(userService.setEnabled(id, false, authentication.getName()));
    }

    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/{id}/reactivate")
    public UserResponse reactivate(@PathVariable Long id, Authentication authentication) {
        return new UserResponse(userService.setEnabled(id, true, authentication.getName()));
    }

    // Force-logout a user everywhere without disabling the account.
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/{id}/force-logout")
    public UserResponse forceLogout(@PathVariable Long id) {
        return new UserResponse(userService.forceLogout(id));
    }
}
