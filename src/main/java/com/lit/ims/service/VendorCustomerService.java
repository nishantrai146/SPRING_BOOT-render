package com.lit.ims.service;

import com.lit.ims.dto.VendorCustomerDTO;
import com.lit.ims.entity.VendorCustomer;
import com.lit.ims.repository.VendorCustomerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorCustomerService {

    private final VendorCustomerRepo repository;
    private final TransactionLogService logService;

    // ✅ Generate Code
    public String generateCode(String type, Long companyId, Long branchId) {
        String prefix = type.equalsIgnoreCase("Vendor") ? "VN" : "CU";

        String maxCode = repository.findMaxCodeByTypeAndCompanyIdAndBranchId(type, companyId, branchId);

        int nextNumber = 1;
        if (maxCode != null) {
            try {
                String numberPart = maxCode.substring(2); // Skip 'VN' or 'CU'
                nextNumber = Integer.parseInt(numberPart) + 1;
            } catch (NumberFormatException ignored) {
            }
        }

        return String.format("%s%04d", prefix, nextNumber);
    }

    // ✅ Create
    public VendorCustomer add(VendorCustomerDTO dto, Long companyId, Long branchId) {
        String code = generateCode(dto.getType(), companyId, branchId);

        VendorCustomer vc = VendorCustomer.builder()
                .code(code)
                .type(dto.getType())
                .name(dto.getName())
                .mobile(dto.getMobile())
                .email(dto.getEmail())
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .pincode(dto.getPincode())
                .status(dto.getStatus())
                .companyId(companyId)
                .branchId(branchId)
                .build();

        VendorCustomer saved = repository.save(vc);

        logService.log("CREATE", "VendorCustomer", saved.getId(),
                "Created " + saved.getType() + ": " + saved.getName() + " with code " + saved.getCode());

        return saved;
    }

    // ✅ Get All
    public List<VendorCustomer> getAll(Long companyId, Long branchId) {
        return repository.findByCompanyIdAndBranchId(companyId, branchId);
    }

    // ✅ Get by ID
    public VendorCustomer getById(Long id, Long companyId, Long branchId) {
        return repository.findById(id)
                .filter(vc -> vc.getCompanyId().equals(companyId) && vc.getBranchId().equals(branchId))
                .orElseThrow(() -> new IllegalArgumentException("Vendor/Customer not found with ID: " + id));
    }

    // ✅ Update
    @Transactional
    public VendorCustomer update(Long id, VendorCustomerDTO dto, Long companyId, Long branchId) {
        VendorCustomer vc = getById(id, companyId, branchId);

        vc.setType(dto.getType());
        vc.setName(dto.getName());
        vc.setMobile(dto.getMobile());
        vc.setEmail(dto.getEmail());
        vc.setCity(dto.getCity());
        vc.setState(dto.getState());
        vc.setAddress(dto.getAddress());
        vc.setPincode(dto.getPincode());
        vc.setStatus(dto.getStatus());

        VendorCustomer updated = repository.save(vc);

        logService.log("UPDATE", "VendorCustomer", updated.getId(),
                "Updated " + updated.getType() + ": " + updated.getName());

        return updated;
    }

    // ✅ Delete
    @Transactional
    public void delete(Long id, Long companyId, Long branchId) {
        VendorCustomer vc = getById(id, companyId, branchId);
        repository.delete(vc);

        logService.log("DELETE", "VendorCustomer", id,
                "Deleted " + vc.getType() + ": " + vc.getName());
    }

    // ✅ Delete Multiple
    @Transactional
    public void deleteMultiple(List<Long> ids, Long companyId, Long branchId) {
        List<VendorCustomer> customers = repository.findAllById(ids).stream()
                .filter(vc -> vc.getCompanyId().equals(companyId) && vc.getBranchId().equals(branchId))
                .toList();

        if (customers.isEmpty()) {
            throw new IllegalArgumentException("No matching Vendor/Customer records found for deletion.");
        }

        repository.deleteAll(customers);

        customers.forEach(vc -> logService.log("DELETE", "VendorCustomer", vc.getId(),
                "Deleted " + vc.getType() + ": " + vc.getName()));
    }

    // ✅ Get Only Vendors
    @Transactional
    public List<VendorCustomer> getAllVendors(Long companyId, Long branchId) {
        return repository.findByTypeAndCompanyIdAndBranchId("Vendor", companyId, branchId);
    }

}
