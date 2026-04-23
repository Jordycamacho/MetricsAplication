package com.fitapp.backend.auth.aplication.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.fitapp.backend.auth.aplication.port.output.UserPersistencePort;
import com.fitapp.backend.auth.domain.model.CustomUserDetails;
import com.fitapp.backend.auth.domain.model.UserModel;

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