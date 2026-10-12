package com.example.project.security;

import com.example.project.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wrapper của User entity để tích hợp với Spring Security.
 * Flatten permissions từ tất cả roles của user thành GrantedAuthority.
 */
public class CustomUserDetails implements UserDetails {

    @Getter
    private final Long id;

    @Getter
    private final String email;

    private final String username;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.enabled = Boolean.TRUE.equals(user.getEnabled());

        Set<GrantedAuthority> auths = new java.util.HashSet<>();

        if (user.getRoles() != null) {
            user.getRoles().forEach(role -> {
                // 1. Thêm Role với tiền tố ROLE_ (hỗ trợ hasRole('ADMIN'))
                if (role.getName() != null) {
                    auths.add(new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()));
                }

                // 2. Thêm Permissions chi tiết (hỗ trợ hasAuthority('USER_READ'))
                if (role.getPermissions() != null) {
                    role.getPermissions().forEach(permission -> {
                        if (permission.getName() != null) {
                            auths.add(new SimpleGrantedAuthority(permission.getName()));
                        }
                    });
                }
            });
        }

        this.authorities = auths;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override public String getPassword()              { return password; }
    @Override public String getUsername()              { return username; }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return enabled; }
}
