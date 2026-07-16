package edu.cit.erag.souvenirpos.shared.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Monotonic counter embedded in issued tokens. Bumping it (password change, admin
     * force-logout, deactivation) instantly invalidates every token minted before the bump,
     * giving server-side revocation without a token blocklist.
     */
    @Column(name = "token_version", nullable = false, columnDefinition = "integer default 0")
    private int tokenVersion = 0;

    /** Disabled accounts cannot log in and their existing tokens are rejected. */
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean enabled = true;

    /** When true the user must set a new password before doing anything else. */
    @Column(name = "must_change_password", nullable = false, columnDefinition = "boolean default false")
    private boolean mustChangePassword = false;

    public User() {
    }

    public User(String name, String username, String passwordHash, Role role) {
        this.name = name;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public int getTokenVersion() {
        return tokenVersion;
    }

    public void setTokenVersion(int tokenVersion) {
        this.tokenVersion = tokenVersion;
    }

    /** Invalidates all currently-issued tokens for this user. */
    public void incrementTokenVersion() {
        this.tokenVersion++;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }
}
