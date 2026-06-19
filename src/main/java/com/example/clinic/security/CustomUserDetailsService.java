package com.example.clinic.security;

import com.example.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Spring Security calls this during login to load the user from the database
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        com.example.clinic.entity.User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + username));

        // Wrap our User entity into Spring Security's UserDetails format
        return new User(
                user.getUsername(),
                user.getPasswordHash(),
                user.getIsActive(),   // account enabled?
                true,                  // account not expired
                true,                  // credentials not expired
                true,                  // account not locked
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}