package com.lit.ims.controller;

import com.lit.ims.dto.LoginRequest;
import com.lit.ims.dto.PermissionDto;
import com.lit.ims.entity.PagePermission;
import com.lit.ims.entity.User;
import com.lit.ims.repository.UserRepository;
import com.lit.ims.security.JwtService;
import com.lit.ims.service.LoginLogService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final LoginLogService loginLogService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest req,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {
        String ipAddress = request.getRemoteAddr();
        String username = req.getUsername();
        String branch = req.getBranch();

        try {
            User user = userRepo.findByUsername(username)
                    .orElseThrow(() -> {
                        loginLogService.log(username, ipAddress, branch, "FAILURE");
                        return new RuntimeException("User not found");
                    });

            if (!encoder.matches(req.getPassword(), user.getPassword())) {
                loginLogService.log(username, ipAddress, branch, "FAILURE");
                throw new RuntimeException("Invalid credentials");
            }

            if (!user.getBranch().equalsIgnoreCase(branch)) {
                loginLogService.log(username, ipAddress, branch, "FAILURE");
                throw new RuntimeException("Invalid branch");
            }

            user.setLastLoginIp(ipAddress);
            user.setLastLoginDateTime(LocalDateTime.now());
            userRepo.save(user);

            loginLogService.log(username, ipAddress, branch, "SUCCESS");

            String token = jwtService.generateToken(user.getUsername());

            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            response.addCookie(cookie);

            List<PermissionDto> permissions = user.getPermissions().stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Login successful");
            responseBody.put("token", token);
            responseBody.put("permissions", permissions);

            return ResponseEntity.ok(responseBody);

        } catch (RuntimeException e) {
            throw e;
        }
    }

    private PermissionDto mapToDto(PagePermission permission) {
        PermissionDto dto = new PermissionDto();
        dto.setPageName(permission.getPageName());
        dto.setCanView(permission.isCanView());
        dto.setCanEdit(permission.isCanEdit());
        return dto;
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Expire immediately
        response.addCookie(cookie);
        return "Logged out";
    }
}
