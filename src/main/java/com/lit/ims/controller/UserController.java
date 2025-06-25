package com.lit.ims.controller;

import com.lit.ims.dto.CreateUserRequest;
import com.lit.ims.dto.UserDto;
import com.lit.ims.entity.Role;
import com.lit.ims.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    public String create(@RequestBody CreateUserRequest request) {
        userService.createUser(request);

        return "User created successfully";
    }
    
    @GetMapping("/check-role")
    public String checkRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return "Current user: " + auth.getName() + ", Roles: " + auth.getAuthorities();
    }
    
    @GetMapping("/all-staff")
    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    public ResponseEntity<List<UserDto>> getAllStaff() {
        List<Role> staffRoles = Arrays.asList(Role.ADMIN, Role.MANAGER, Role.USER);
        List<UserDto> users = userService.getUsersByRoles(staffRoles);
        return ResponseEntity.ok(users);
    }


}
