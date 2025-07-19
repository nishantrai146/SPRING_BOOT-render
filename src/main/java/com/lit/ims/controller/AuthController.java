package com.lit.ims.controller;

import com.lit.ims.dto.LoginRequest;
import com.lit.ims.dto.PermissionDto;
import com.lit.ims.entity.Branch;
import com.lit.ims.entity.PagePermission;
import com.lit.ims.entity.User;
import com.lit.ims.exception.ResourceNotFoundException;
import com.lit.ims.repository.BranchRepository;
import com.lit.ims.repository.UserRepository;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.security.JwtService;
import com.lit.ims.service.LoginLogService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AuthController {

    private final UserRepository userRepo;
    private final BranchRepository branchRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final LoginLogService loginLogService;

    /**
     * ✅ Step 1: Login with username & password → Get list of branches for this user
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @RequestBody LoginRequest req,
            HttpServletRequest request) {

        String ipAddress = request.getRemoteAddr();
        String username = req.getUsername();

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> {
                    loginLogService.log(username, ipAddress, "FAILURE");
                    return new ResourceNotFoundException("User not found.");
                });

        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            loginLogService.log(username, ipAddress, "FAILURE");
            throw new ResourceNotFoundException("Invalid credentials.");
        }

        loginLogService.log(username, ipAddress, "SUCCESS");

        // ✅ Return branches assigned to this user
        List<Map<String, Object>> branches = user.getBranches().stream()
                .collect(Collectors.toMap(
                        Branch::getId, // use branch ID as key
                        branch -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", branch.getId());
                            map.put("code", branch.getCode());
                            map.put("name", branch.getName());
                            return map;
                        },
                        (existing, replacement) -> existing // if duplicate ID, keep one
                ))
                .values()
                .stream()
                .collect(Collectors.toList());


        Map<String, Object> data = new HashMap<>();
        data.put("companyId", user.getCompany().getId());
        data.put("companyName", user.getCompany().getName());
        data.put("branches", branches);
        data.put("username", user.getUsername());
        data.put("userId", user.getId());

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Login successful", data)
        );
    }

    /**
     * ✅ Step 2: Select Branch → Get JWT token
     */
    @PostMapping("/select-branch")
    public ResponseEntity<ApiResponse<Map<String, Object>>> selectBranch(
            @RequestParam String username,
            @RequestParam Long branchId,
            HttpServletResponse response) {

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean hasAccess = user.getBranches().stream()
                .anyMatch(branch -> branch.getId().equals(branchId));

        if (!hasAccess) {
            throw new ResourceNotFoundException("User does not have access to this branch.");
        }

        Branch branch = branchRepo.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        String token = jwtService.generateToken(
                user.getId(),
                user.getUsername(),
                user.getCompany().getId(),
                branch.getId(),
                user.getRole().name()
        );

        // ✅ Set token in secure cookie
        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(false) // ✅ Should be true in production with HTTPS
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        List<PermissionDto> permissions = user.getPermissions().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("permissions", permissions);
        data.put("branchId", branch.getId());
        data.put("branchName", branch.getName());

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Branch selected and token generated.", data)
        );
    }

    /**
     * ✅ Logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Logged out successfully", "Logged out")
        );
    }

    private PermissionDto mapToDto(PagePermission permission) {
        PermissionDto dto = new PermissionDto();
        dto.setPageName(permission.getPageName());
        dto.setCanView(permission.isCanView());
        dto.setCanEdit(permission.isCanEdit());
        return dto;
    }
}
