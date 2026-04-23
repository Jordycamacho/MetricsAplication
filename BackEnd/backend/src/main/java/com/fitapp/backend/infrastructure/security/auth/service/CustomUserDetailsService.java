package com.fitapp.backend.infrastructure.security.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.fitapp.backend.infrastructure.security.auth.model.CustomUserDetails;
import com.fitapp.backend.user.aplication.port.output.UserPersistencePort;
import com.fitapp.backend.user.domain.model.UserModel;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserPersistencePort userRepository;

    public CustomUserDetailsService(UserPersistencePort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
        
        return new CustomUserDetails(user);
    }
}