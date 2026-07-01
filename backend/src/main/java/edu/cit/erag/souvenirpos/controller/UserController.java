package edu.cit.erag.souvenirpos.controller;

import edu.cit.erag.souvenirpos.dto.UserCreateRequest;
import edu.cit.erag.souvenirpos.dto.UserResponse;
import edu.cit.erag.souvenirpos.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
}
