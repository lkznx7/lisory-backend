package com.lisory.backend.auth.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "auth_users")
public class AuthEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private ROLES role;

    @Column(name = "is_active")
    private Boolean isActive;

    public AuthEntity() {}

    public AuthEntity(String email, String password) {
        this.email = email;
        this.password = password;
        this.role = ROLES.USER;
        this.isActive = true;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (getRole() == null) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + getRole().name()));
    }
    @Override
    public String getUsername() {
        return this.email;
    }
    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(isActive);
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

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public ROLES getRole() { return role; }
    public void setRole(ROLES role) { this.role = role; }
    public Boolean getActive() { return isActive; }
    public void setActive(Boolean active) { isActive = active; }
}
