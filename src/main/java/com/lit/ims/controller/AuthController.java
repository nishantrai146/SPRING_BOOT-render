package com.lit.ims.controller;

import com.lit.ims.dto.LoginRequest;
import com.lit.ims.dto.PermissionDto;
import com.lit.ims.entity.Branch;
import com.lit.ims.entity.PagePermission;
import com.lit.ims.entity.User;
import com.lit.ims.repository.BranchRepository;
import com.lit.ims.repository.UserRepository;
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

import java.time.LocalDateTime;
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

    // ✅ Step 1: Login with username & password → Get list of branches for this user
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest req,
                                                     HttpServletRequest request) {

        String ipAddress = request.getRemoteAddr();
        String username = req.getUsername();

        Map<String, Object> responseBody = new HashMap<>();

        User user = userRepo.findByUsername(username)
                .orElse(null);

        if (user == null) {
            loginLogService.log(username, ipAddress, "FAILURE");
            responseBody.put("message", "Login failed: User not found.");
            return ResponseEntity.status(401).body(responseBody);
        }

        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            loginLogService.log(username, ipAddress, "FAILURE");
            responseBody.put("message", "Login failed: Invalid credentials.");
            return ResponseEntity.status(401).body(responseBody);
        }

        loginLogService.log(username, ipAddress, "SUCCESS");

        // ✅ Return branches assigned to this user
        List<Map<String, Object>> branches = user.getBranches().stream()
                .map(branch -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", branch.getId());
                    map.put("code", branch.getCode());
                    map.put("name", branch.getName());
                    return map;
                })
                .collect(Collectors.toList());

        responseBody.put("message", "Login successful");
        responseBody.put("companyId", user.getCompany().getId());
        responseBody.put("companyName", user.getCompany().getName());
        responseBody.put("branches", branches);
        responseBody.put("username", user.getUsername());
        responseBody.put("userId", user.getId());

        return ResponseEntity.ok(responseBody);
    }

    // ✅ Step 2: Select Branch → Get JWT token
    @PostMapping("/select-branch")
    public ResponseEntity<Map<String, Object>> selectBranch(
            @RequestParam String username,
            @RequestParam Long branchId,
            HttpServletResponse response) {

        Map<String, Object> responseBody = new HashMap<>();

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean hasAccess = user.getBranches().stream()
                .anyMatch(branch -> branch.getId().equals(branchId));

        if (!hasAccess) {
            throw new RuntimeException("User does not have access to this branch.");
        }

        Branch branch = branchRepo.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

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
                .secure(false) // ✅ true in production with HTTPS
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax") // or "Strict" or "None"
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        List<PermissionDto> permissions = user.getPermissions().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        responseBody.put("message", "Branch selected and token generated.");
        responseBody.put("token", token);
        responseBody.put("permissions", permissions);
        responseBody.put("branchId", branch.getId());
        responseBody.put("branchName", branch.getName());

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "Logged out";
    }

    private PermissionDto mapToDto(PagePermission permission) {
        PermissionDto dto = new PermissionDto();
        dto.setPageName(permission.getPageName());
        dto.setCanView(permission.isCanView());
        dto.setCanEdit(permission.isCanEdit());
        return dto;
    }
}
