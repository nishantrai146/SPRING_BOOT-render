package com.lit.ims.controller;

import com.lit.ims.dto.CreateUserRequest;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

import com.lit.ims.entity.Role;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ✅ Create User API
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    public ResponseEntity<ApiResponse> createUser(@RequestBody CreateUserRequest request,
                                                  @RequestAttribute("companyId") Long companyId,
                                                  @RequestAttribute("branchId") Long branchId) {
        return userService.createUser(request, companyId, branchId);
    }

    // ✅ Check Role (Debugging)
    @GetMapping("/check-role")
    public ResponseEntity<ApiResponse> checkRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String info = "Current user: " + auth.getName() + ", Roles: " + auth.getAuthorities();
        return ResponseEntity.ok(ApiResponse.builder()
                .status(true)
                .message("Fetched current user roles")
                .data(info)
                .build());
    }

    // ✅ Get All Staff Users (ADMIN, MANAGER, USER)
    @GetMapping("/all-staff")
    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    public ResponseEntity<ApiResponse> getAllStaff(@RequestAttribute("companyId") Long companyId,
                                                   @RequestAttribute("branchId") Long branchId) {
        List<Role> staffRoles = Arrays.asList(Role.ADMIN, Role.MANAGER, Role.USER);
        return userService.getUsersByRolesAndCompanyBranch(staffRoles, companyId, branchId);
    }

    // ✅ Delete User
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id,
                                                  @RequestAttribute("companyId") Long companyId,
                                                  @RequestAttribute("branchId") Long branchId) {
        return userService.deleteUser(id, companyId, branchId);
    }
}
