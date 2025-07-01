package com.lit.ims.service;

import com.lit.ims.entity.User;
import com.lit.ims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            User user = userRepo.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

            return mapToUserDetails(user);

        } catch (IncorrectResultSizeDataAccessException e) {
            // Log a warning that duplicates exist
            log.warn("Multiple users found with username '{}'. Using the first match.", username);

            List<User> users = userRepo.findAll().stream()
                    .filter(u -> u.getUsername().equals(username))
                    .toList();

            if (users.isEmpty()) {
                throw new UsernameNotFoundException("User not found with username: " + username);
            }

            return mapToUserDetails(users.get(0));
        }
    }

    private UserDetails mapToUserDetails(User user) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();
    }
}
