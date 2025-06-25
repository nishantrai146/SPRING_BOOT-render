package com.lit.ims.service;

import com.lit.ims.entity.User;
import com.lit.ims.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired private UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) {
        try {
            var user = userRepo.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                    .password(user.getPassword())
                    .authorities("ROLE_" + user.getRole().name())
                    .build();
        } catch (IncorrectResultSizeDataAccessException e) {
            // Handle case where multiple users with same username exist
            // This is a temporary workaround until the duplicates are cleaned up
            List<User> users = userRepo.findAll().stream()
                    .filter(u -> u.getUsername().equals(username))
                    .toList();
            
            if (users.isEmpty()) {
                throw new UsernameNotFoundException("User not found");
            }
            
            // Use the first user found as a fallback
            User user = users.get(0);
            return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                    .password(user.getPassword())
                    .authorities("ROLE_" + user.getRole().name())
                    .build();
        }
    }
}
