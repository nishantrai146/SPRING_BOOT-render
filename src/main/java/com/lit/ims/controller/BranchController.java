package com.lit.ims.controller;

import com.lit.ims.dto.BranchDTO;
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
    public ResponseEntity<BranchDTO> saveBranch(@RequestBody BranchDTO dto) {
        return ResponseEntity.ok(branchService.saveBranch(dto));
    }

    @GetMapping("/by-company")
    public ResponseEntity<List<BranchDTO>> getBranchesByCompany(HttpServletRequest request) {
        String token = jwtService.extractTokenFromRequest(request);
        Long companyId = jwtService.extractCompanyId(token);

        return ResponseEntity.ok(branchService.getBranchesByCompany(companyId));
    }

}

