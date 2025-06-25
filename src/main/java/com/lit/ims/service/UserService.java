package com.lit.ims.service;

import com.lit.ims.dto.*;
import com.lit.ims.entity.*;
import com.lit.ims.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final PagePermissionRepository permRepo;
    private final PasswordEncoder encoder;
    private final HttpServletRequest request;
    private final TransactionLogService logService; // ✅ Injected

    public void createOwnerIfNotExists() {
        if (userRepo.findByUsername("owner").isEmpty()) {
            User owner = new User();
            owner.setUsername("owner");
            owner.setEmail("owner@example.com");
            owner.setPassword(encoder.encode("owner123"));
            owner.setRole(Role.OWNER);
            owner.setBranch("HQ");
            owner.setDepartment("Management");
            owner.setStatus(Status.ACTIVE);
            owner.setLastLoginDateTime(LocalDateTime.now());
            userRepo.save(owner);

            // ✅ Log owner creation
            logService.log("CREATE", "User", null, "Owner account created automatically.");
        }
    }

    public void createUser(CreateUserRequest req) {
        if (userRepo.findByUsername(req.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists. Please choose a different username.");
        }

        Role selectedRole;
        try {
            selectedRole = Role.valueOf(req.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Role must be ADMIN, MANAGER, or USER.");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setRole(selectedRole);
        user.setBranch(req.getBranch());
        user.setDepartment(req.getDepartment());
        user.setStatus(Status.ACTIVE);
        user.setLastLoginIp(request.getRemoteAddr());
        user.setLastLoginDateTime(LocalDateTime.now());

        List<PagePermission> permissions = new ArrayList<>();
        for (PermissionDto dto : req.getPermissions()) {
            PagePermission p = new PagePermission();
            p.setPageName(dto.getPageName());
            p.setCanView(dto.isCanView());
            p.setCanEdit(dto.isCanEdit());
            p.setUser(user);
            permissions.add(p);
        }

        user.setPermissions(permissions);
        User saved = userRepo.save(user);

        // ✅ Log user creation
        logService.log("CREATE", "User", saved.getId(), "Created user: " + saved.getUsername());
    }

    public List<UserDto> getUsersByRoles(List<Role> roles) {
        List<User> users = userRepo.findByRoleIn(roles);
        return users.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .branch(user.getBranch())
                .department(user.getDepartment())
                .status(user.getStatus())
                .lastLoginDateTime(user.getLastLoginDateTime())
                .lastLoginIp(user.getLastLoginIp())
                .build();
    }
}
