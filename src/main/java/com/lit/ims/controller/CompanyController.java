package com.lit.ims.controller;

import com.lit.ims.dto.CompanyDTO;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<CompanyDTO>> saveCompany(@RequestBody CompanyDTO dto) {
        CompanyDTO saved = companyService.saveCompany(dto);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Company saved successfully", saved)
        );
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CompanyDTO>>> getAllCompanies() {
        List<CompanyDTO> companies = companyService.getAllCompanies();
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Fetched all companies successfully", companies)
        );
    }
}
