package com.fitapp.backend.domain.model;

import com.fitapp.backend.infrastructure.persistence.entity.enums.Role;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
    private String email;
    private String fullName;
    private String profileImage;
    private String password;
    private Role role;
    private LocalDateTime lastLogin;
    private boolean isActive;
    private Integer maxRoutines;
    private SubscriptionModel subscription;

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public List<SimpleGrantedAuthority> getGrantedAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));

        if (subscription != null && subscription.getEndDate().isAfter(LocalDate.now())) {
            authorities.add(new SimpleGrantedAuthority(
                    "ROLE_" + subscription.getType().name() + "_USER"));

            if (subscription.getType() == SubscriptionType.PREMIUM) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADVANCED_ANALYTICS"));
            }
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_FREE_USER"));
        }

        return authorities;
    }
}
