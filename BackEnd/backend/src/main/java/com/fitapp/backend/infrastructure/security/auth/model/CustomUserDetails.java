package com.fitapp.backend.infrastructure.security.auth.model;

import java.time.LocalDate;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;


public class CustomUserDetails implements UserDetails {
    
    private final UserModel user;
    
    public CustomUserDetails(UserModel user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getGrantedAuthorities();
    }
    
    @Override
    public String getPassword() {
        return user.getPassword(); 
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        if (user.getSubscription() != null) {
            return user.getSubscription().getEndDate().isAfter(LocalDate.now());
        }
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
    
    public Long getUserId() {
        return user.getId();
    }
    
    public SubscriptionType getSubscriptionType() {
        return user.getSubscription() != null ? 
            user.getSubscription().getType() : SubscriptionType.FREE;
    }
}