package com.lit.ims.controller;

import com.lit.ims.dto.CompanyDTO;
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
    public ResponseEntity<CompanyDTO> saveCompany(@RequestBody CompanyDTO dto) {
        return ResponseEntity.ok(companyService.saveCompany(dto));
    }

    @GetMapping("/all")
    public ResponseEntity<List<CompanyDTO>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }
}

