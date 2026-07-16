package edu.cit.erag.souvenirpos.shared.security;

import edu.cit.erag.souvenirpos.shared.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * UserDetails backed by the domain {@link User}, carrying the extra fields the JWT filter
 * needs to enforce revocation ({@code tokenVersion}) and deactivation ({@code enabled})
 * on every request — Spring's default User type can't hold these.
 */
public class AppUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String passwordHash;
    private final int tokenVersion;
    private final boolean enabled;
    private final boolean mustChangePassword;
    private final List<GrantedAuthority> authorities;

    public AppUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.passwordHash = user.getPasswordHash();
        this.tokenVersion = user.getTokenVersion();
        this.enabled = user.isEnabled();
        this.mustChangePassword = user.isMustChangePassword();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    public Long getId() {
        return id;
    }

    public int getTokenVersion() {
        return tokenVersion;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
