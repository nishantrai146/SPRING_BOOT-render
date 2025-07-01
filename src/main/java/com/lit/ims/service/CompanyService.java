package com.lit.ims.service;

import com.lit.ims.dto.CompanyDTO;
import com.lit.ims.entity.Company;
import com.lit.ims.exception.ResourceNotFoundException;
import com.lit.ims.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyDTO saveCompany(CompanyDTO dto) {
        // Check if company code already exists
        if (companyRepository.findByCode(dto.getCode()).isPresent()) {
            throw new ResourceNotFoundException("Company code already exists: " + dto.getCode());
        }

        Company company = new Company();
        company.setCode(dto.getCode());
        company.setName(dto.getName());
        company.setAddress(dto.getAddress());
        company.setPhone(dto.getPhone());
        company.setEmail(dto.getEmail());

        Company saved = companyRepository.save(company);

        dto.setId(saved.getId());
        return dto;
    }

    public List<CompanyDTO> getAllCompanies() {
        return companyRepository.findAll().stream().map(c -> {
            CompanyDTO dto = new CompanyDTO();
            dto.setId(c.getId());
            dto.setCode(c.getCode());
            dto.setName(c.getName());
            dto.setAddress(c.getAddress());
            dto.setPhone(c.getPhone());
            dto.setEmail(c.getEmail());
            return dto;
        }).collect(Collectors.toList());
    }
}
