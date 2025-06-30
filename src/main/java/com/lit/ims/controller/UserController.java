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
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    public ResponseEntity<?> create(@RequestBody CreateUserRequest request,
                                    @RequestAttribute("companyId") Long companyId,
                                    @RequestAttribute("branchId") Long branchId) {
        try {
            userService.createUser(request, companyId, branchId);
            return ResponseEntity.ok(
                    Map.of("status", "success", "message", "User created successfully")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", "error", "message", e.getMessage())
            );
        }
    }


    @GetMapping("/check-role")
    public String checkRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return "Current user: " + auth.getName() + ", Roles: " + auth.getAuthorities();
    }

    @GetMapping("/all-staff")
    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    public ResponseEntity<?> getAllStaff(@RequestAttribute("companyId") Long companyId,
                                         @RequestAttribute("branchId") Long branchId) {
        List<Role> staffRoles = Arrays.asList(Role.ADMIN, Role.MANAGER, Role.USER);
        List<UserDto> users = userService.getUsersByRolesAndCompanyBranch(staffRoles, companyId, branchId);
        return ResponseEntity.ok(Map.of("users", users));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id,
                                        @RequestAttribute("companyId") Long companyId,
                                        @RequestAttribute("branchId") Long branchId) {
        try {
            userService.deleteUser(id, companyId, branchId);
            return ResponseEntity.ok(Map.of("status", "success", "message", "User deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }




}
