package com.lit.ims.service;

import com.lit.ims.dto.*;
import com.lit.ims.entity.*;
import com.lit.ims.exception.ResourceNotFoundException;
import com.lit.ims.repository.*;
import com.lit.ims.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    private final TransactionLogService logService;
    private final CompanyRepository companyRepo;
    private final BranchRepository branchRepo;

    // ✅ Initial setup for Owner, Company, Branch
    public void createOwnerIfNotExists() {
        if (userRepo.findByUsername("owner").isEmpty()) {

            Company defaultCompany = companyRepo.findByCode("LIT").orElseGet(() -> {
                Company company = new Company();
                company.setCode("LIT");
                company.setName("LIT INDIA");
                company.setAddress("A-124 Sector 80");
                company.setPhone("0000000000");
                company.setEmail("default@example.com");
                return companyRepo.save(company);
            });

            Branch defaultBranch = branchRepo.findByCodeAndCompanyId("HQ", defaultCompany.getId()).orElseGet(() -> {
                Branch branch = new Branch();
                branch.setCode("HQ");
                branch.setName("A-124,LIT INDIA Pvt Ltd");
                branch.setAddress("A-124 Sector 80");
                branch.setPhone("0000000000");
                branch.setEmail("hq@example.com");
                branch.setCompany(defaultCompany);
                return branchRepo.save(branch);
            });

            User owner = new User();
            owner.setUsername("owner");
            owner.setEmail("owner@example.com");
            owner.setPassword(encoder.encode("owner123"));
            owner.setRole(Role.OWNER);
            owner.setDepartment("Management");
            owner.setStatus(Status.ACTIVE);
            owner.setLastLoginIp("SYSTEM"); // ✅ Use "SYSTEM" at startup
            owner.setLastLoginDateTime(LocalDateTime.now());
            owner.setCompany(defaultCompany);
            owner.setBranches(List.of(defaultBranch)); // ✅ Assign branch

            User savedOwner = userRepo.save(owner);

            List<String> allPages = List.of("Business Partner",
                    "Vendor Item Master",
                    "Inventory Audit Report",
                    "Part Master",
                    "BOM Master",
                    "Type Master",
                    "Group Master",
                    "Item Master",
                    "Warehouse Master",
                    "Store Material Inward",
                    "IQC",
                    "Material Issue Request",
                    "Material Issue Transfer",
                    "Material Receipt",
                    "Production Material Usage",
                    "WIP Return",
                    "Inventory Report",
                    "Transaction Report",
                    "Production Report",
                    "User Management",
                    "Role Management",
                    "System Settings",
                    "Activity Logs",
                    "Approve Items Quantity");
            List<PagePermission> permissions = allPages.stream().map(p -> {
                PagePermission perm = new PagePermission();
                perm.setPageName(p);
                perm.setCanView(true);
                perm.setCanEdit(true);
                perm.setUser(savedOwner);
                return perm;
            }).toList();
            savedOwner.setPermissions(permissions);
            userRepo.save(savedOwner);
            logService.log("CREATE", "User", null, "Owner account created automatically.");
        }
    }

    // ✅ Create normal users with branches
    public ResponseEntity<ApiResponse> createUser(CreateUserRequest req, Long companyId, Long branchId) {
        if (userRepo.findByUsername(req.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists. Please choose a different username.");
        }

        Role selectedRole;
        try {
            selectedRole = Role.valueOf(req.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Role must be ADMIN, MANAGER, or USER.");
        }

        Company company = companyRepo.findById(companyId).orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + companyId));

        Branch defaultBranch = branchRepo.findById(branchId).orElseThrow(() -> new ResourceNotFoundException("Branch not found with ID: " + branchId));

        List<Branch> branches = new ArrayList<>();
        if (req.getBranchIds() != null && !req.getBranchIds().isEmpty()) {
            branches = branchRepo.findAllById(req.getBranchIds());
            if (branches.isEmpty()) {
                throw new RuntimeException("At least one valid branch must be selected.");
            }
            for (Branch b : branches) {
                if (!b.getCompany().getId().equals(company.getId())) {
                    throw new RuntimeException("Branch " + b.getName() + " does not belong to the selected company.");
                }
            }
        } else {
            branches.add(defaultBranch);
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setRole(selectedRole);
        user.setDepartment(req.getDepartment());
        user.setStatus(Status.valueOf(req.getStatus().toUpperCase()));
        user.setLastLoginIp(request.getRemoteAddr());
        user.setLastLoginDateTime(LocalDateTime.now());
        user.setCompany(company);
        user.setBranches(branches);

        List<PagePermission> permissions = req.getPermissions().stream().map(dto -> {
            PagePermission p = new PagePermission();
            p.setPageName(dto.getPageName());
            p.setCanView(dto.isCanView());
            p.setCanEdit(dto.isCanEdit());
            p.setUser(user);
            return p;
        }).collect(Collectors.toList());

        user.setPermissions(permissions);

        User saved = userRepo.save(user);

        logService.log("CREATE", "User", saved.getId(), "Created user: " + saved.getUsername());

        return ResponseEntity.ok(ApiResponse.builder().status(true).message("User created successfully").data(saved.getId()).build());
    }

    // ✅ Get users by role
    public ResponseEntity<ApiResponse> getUsersByRolesAndCompanyBranch(List<Role> roles, Long companyId, Long branchId) {
        List<User> users = userRepo.findByRoleInAndCompanyIdAndBranchId(roles, companyId, branchId);
        List<UserDto> userDtos = users.stream().map(this::mapToDto).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.builder().status(true).message("Users fetched successfully").data(userDtos).build());
    }

    // ✅ Mapping User to DTO
    private UserDto mapToDto(User user) {
        return UserDto.builder().id(user.getId()).username(user.getUsername()).email(user.getEmail()).role(user.getRole()).department(user.getDepartment()).status(user.getStatus()).lastLoginDateTime(user.getLastLoginDateTime()).lastLoginIp(user.getLastLoginIp()).companyId(user.getCompany() != null ? user.getCompany().getId() : null).companyName(user.getCompany() != null ? user.getCompany().getName() : null).branches(user.getBranches() != null ? user.getBranches().stream().map(branch -> BranchInfoDto.builder().id(branch.getId()).name(branch.getName()).code(branch.getCode()).build()).collect(Collectors.toList()) : List.of()).permissions(user.getPermissions() != null ? user.getPermissions().stream().map(p -> PermissionDto.builder().pageName(p.getPageName()).canView(p.isCanView()).canEdit(p.isCanEdit()).build()).toList() : List.of()).build();
    }

    // ✅ Delete user with validation
    public ResponseEntity<ApiResponse> deleteUser(Long userId, Long companyId, Long branchId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (!user.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("User does not belong to your company.");
        }

        boolean branchMatch = user.getBranches().stream().anyMatch(branch -> branch.getId().equals(branchId));
        if (!branchMatch) {
            throw new RuntimeException("User does not belong to your branch.");
        }

        userRepo.delete(user);

        logService.log("DELETE", "User", userId, "Deleted user: " + user.getUsername());

        return ResponseEntity.ok(ApiResponse.builder().status(true).message("User deleted successfully").data(null).build());
    }

    public ResponseEntity<ApiResponse> getUserById(Long userId, Long companyId, Long branchId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (!user.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("User does not belong to your company.");
        }

        boolean branchMatch = user.getBranches().stream().anyMatch(branch -> branch.getId().equals(branchId));
        if (!branchMatch) {
            throw new RuntimeException("User does not belong to your branch.");
        }

        logService.log("VIEW", "User", userId, "Fetched user details: " + user.getUsername());

        UserDto dto = mapToDto(user);

        return ResponseEntity.ok(ApiResponse.builder().status(true).message("User fetched successfully").data(dto).build());
    }

    public ResponseEntity<ApiResponse> updateUser(Long userId, CreateUserRequest req, Long companyId, Long branchId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (!user.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("User does not belong to your company.");
        }

        boolean branchMatch = user.getBranches().stream().anyMatch(branch -> branch.getId().equals(branchId));
        if (!branchMatch) {
            throw new RuntimeException("User does not belong to your branch.");
        }

        // Update user fields
        user.setEmail(req.getEmail());
        user.setDepartment(req.getDepartment());
        user.setStatus(Status.valueOf(req.getStatus().toUpperCase()));

        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(encoder.encode(req.getPassword()));
        }

        Role selectedRole;
        try {
            selectedRole = Role.valueOf(req.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Role must be ADMIN, MANAGER, or USER.");
        }
        user.setRole(selectedRole);

        List<Branch> branches = new ArrayList<>();
        if (req.getBranchIds() != null && !req.getBranchIds().isEmpty()) {
            branches = branchRepo.findAllById(req.getBranchIds());
            if (branches.isEmpty()) {
                throw new RuntimeException("At least one valid branch must be selected.");
            }
            for (Branch b : branches) {
                if (!b.getCompany().getId().equals(companyId)) {
                    throw new RuntimeException("Branch " + b.getName() + " does not belong to the selected company.");
                }
            }
        } else {
            // If not provided, keep existing branches
            branches = user.getBranches();
        }

        user.setBranches(branches);

        // ✅ Clear and replace permissions (orphan-safe)
        if (user.getPermissions() == null) {
            user.setPermissions(new ArrayList<>());
        } else {
            user.getPermissions().clear(); // Orphan-safe removal
        }

        List<PagePermission> newPermissions = req.getPermissions().stream().map(dto -> {
            PagePermission p = new PagePermission();
            p.setPageName(dto.getPageName());
            p.setCanView(dto.isCanView());
            p.setCanEdit(dto.isCanEdit());
            p.setUser(user);
            return p;
        }).collect(Collectors.toList());

        user.getPermissions().addAll(newPermissions);

        User saved = userRepo.save(user);

        logService.log("UPDATE", "User", saved.getId(), "Updated user details: " + saved.getUsername());

        return ResponseEntity.ok(ApiResponse.builder().status(true).message("User updated successfully").data(saved.getId()).build());
    }

}
