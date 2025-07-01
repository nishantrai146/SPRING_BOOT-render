package com.lit.ims.controller;

import com.lit.ims.dto.BranchDTO;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.security.JwtService;
import com.lit.ims.service.BranchService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branch")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;
    private final JwtService jwtService;

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<BranchDTO>> saveBranch(@RequestBody BranchDTO dto) {
        BranchDTO saved = branchService.saveBranch(dto);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Branch saved successfully", saved)
        );
    }

    @GetMapping("/by-company")
    public ResponseEntity<ApiResponse<List<BranchDTO>>> getBranchesByCompany(HttpServletRequest request) {
        String token = jwtService.extractTokenFromRequest(request);
        Long companyId = jwtService.extractCompanyId(token);

        List<BranchDTO> branches = branchService.getBranchesByCompany(companyId);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Fetched branches successfully", branches)
        );
    }
}
