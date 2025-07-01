package com.lit.ims.service;

import com.lit.ims.dto.BranchDTO;
import com.lit.ims.entity.Branch;
import com.lit.ims.entity.Company;
import com.lit.ims.exception.ResourceNotFoundException;
import com.lit.ims.repository.BranchRepository;
import com.lit.ims.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;
    private final CompanyRepository companyRepository;

    public BranchDTO saveBranch(BranchDTO dto) {
        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + dto.getCompanyId()));

        Branch branch = new Branch();
        branch.setCode(dto.getCode());
        branch.setName(dto.getName());
        branch.setAddress(dto.getAddress());
        branch.setPhone(dto.getPhone());
        branch.setEmail(dto.getEmail());
        branch.setCompany(company);

        Branch saved = branchRepository.save(branch);

        dto.setId(saved.getId());
        return dto;
    }

    public List<BranchDTO> getBranchesByCompany(Long companyId) {
        return branchRepository.findByCompanyId(companyId).stream().map(b -> {
            BranchDTO dto = new BranchDTO();
            dto.setId(b.getId());
            dto.setCode(b.getCode());
            dto.setName(b.getName());
            dto.setAddress(b.getAddress());
            dto.setPhone(b.getPhone());
            dto.setEmail(b.getEmail());
            dto.setCompanyId(b.getCompany().getId());
            return dto;
        }).collect(Collectors.toList());
    }
}
