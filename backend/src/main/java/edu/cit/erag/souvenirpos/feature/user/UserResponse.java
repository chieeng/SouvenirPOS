package edu.cit.erag.souvenirpos.feature.user;

import edu.cit.erag.souvenirpos.shared.domain.User;

public class UserResponse {

    private Long id;
    private String name;
    private String username;
    private String role;

    public UserResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.username = user.getUsername();
        this.role = user.getRole().name();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
