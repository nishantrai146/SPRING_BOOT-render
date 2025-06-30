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
    private final TransactionLogService logService;
    private final CompanyRepository companyRepo;
    private final BranchRepository branchRepo;

    // ✅ Initial setup for Owner, Company, Branch
    public void createOwnerIfNotExists() {
        if (userRepo.findByUsername("owner").isEmpty()) {

            Company defaultCompany = companyRepo.findByCode("LIT")
                    .orElseGet(() -> {
                        Company company = new Company();
                        company.setCode("LIT");
                        company.setName("LIT INDIA");
                        company.setAddress("A-124 Sector 80");
                        company.setPhone("0000000000");
                        company.setEmail("default@example.com");
                        return companyRepo.save(company);
                    });

            Branch defaultBranch = branchRepo.findByCodeAndCompanyId("HQ", defaultCompany.getId())
                    .orElseGet(() -> {
                        Branch branch = new Branch();
                        branch.setCode("HQ");
                        branch.setName("Headquarters");
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

            userRepo.save(owner);

            logService.log("CREATE", "User", null, "Owner account created automatically.");
        }
    }

    // ✅ Create normal users with branches
    public void createUser(CreateUserRequest req, Long companyId, Long branchId) {
        if (userRepo.findByUsername(req.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists. Please choose a different username.");
        }

        Role selectedRole;
        try {
            selectedRole = Role.valueOf(req.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Role must be ADMIN, MANAGER, or USER.");
        }

        // ✅ Fetch company from JWT token
        Company company = companyRepo.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        // ✅ Fetch branch from JWT token (Ensures at least the logged-in branch is assigned)
        Branch defaultBranch = branchRepo.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found with ID: " + branchId));

        // ✅ Fetch and validate other branches if user selected
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
            branches.add(defaultBranch); // ✅ If no branches provided, assign current branch
        }

        // ✅ Create User
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setRole(selectedRole);
        user.setDepartment(req.getDepartment());
        user.setStatus(Status.ACTIVE);
        user.setLastLoginIp(request.getRemoteAddr());
        user.setLastLoginDateTime(LocalDateTime.now());
        user.setCompany(company);
        user.setBranches(branches);

        // ✅ Page permissions
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
    }

    // ✅ Get users by role
    public List<UserDto> getUsersByRolesAndCompanyBranch(List<Role> roles, Long companyId, Long branchId) {
        List<User> users = userRepo.findByRoleInAndCompanyIdAndBranchId(roles, companyId, branchId);
        return users.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ✅ Mapping User to DTO
    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .department(user.getDepartment())
                .status(user.getStatus())
                .lastLoginDateTime(user.getLastLoginDateTime())
                .lastLoginIp(user.getLastLoginIp())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .branchIds(user.getBranches() != null
                        ? user.getBranches().stream().map(Branch::getId).collect(Collectors.toList())
                        : Collections.emptyList())
                .build();
    }

    public void deleteUser(Long userId, Long companyId, Long branchId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // ✅ Validate company
        if (!user.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("User does not belong to your company.");
        }

        // ✅ Validate branch
        boolean branchMatch = user.getBranches().stream()
                .anyMatch(branch -> branch.getId().equals(branchId));
        if (!branchMatch) {
            throw new RuntimeException("User does not belong to your branch.");
        }

        userRepo.delete(user);

        logService.log("DELETE", "User", userId, "Deleted user: " + user.getUsername());
    }

}
