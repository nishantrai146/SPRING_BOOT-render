package com.lit.ims.service;

import com.lit.ims.dto.VendorCustomerDTO;
import com.lit.ims.entity.VendorCustomer;
import com.lit.ims.repository.VendorCustomerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VendorCustomerService {

    @Autowired
    private VendorCustomerRepo repository;

    @Autowired
    private TransactionLogService logService;

    // ✅ Create
    public VendorCustomer add(VendorCustomerDTO dto, Long companyId, Long branchId) {
        VendorCustomer vc = new VendorCustomer();
        vc.setType(dto.getType());
        vc.setName(dto.getName());
        vc.setMobile(dto.getMobile());
        vc.setEmail(dto.getEmail());
        vc.setCity(dto.getCity());
        vc.setState(dto.getState());
        vc.setAddress(dto.getAddress());
        vc.setPincode(dto.getPincode());
        vc.setStatus(dto.getStatus());
        vc.setCompanyId(companyId);
        vc.setBranchId(branchId);

        VendorCustomer saved = repository.save(vc);

        logService.log("CREATE", "VendorCustomer", saved.getId(),
                "Created " + saved.getType() + ": " + saved.getName());

        return saved;
    }

    // ✅ Get all for company/branch
    public List<VendorCustomer> getAll(Long companyId, Long branchId) {
        return repository.findByCompanyIdAndBranchId(companyId, branchId);
    }

    // ✅ Get by ID (company/branch enforced)
    public VendorCustomer getById(Long id, Long companyId, Long branchId) {
        return repository.findById(id)
                .filter(vc -> vc.getCompanyId().equals(companyId) && vc.getBranchId().equals(branchId))
                .orElseThrow(() -> new RuntimeException("Vendor/Customer not found"));
    }

    // ✅ Update (company/branch enforced)
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

    // ✅ Delete (company/branch enforced)
    public void delete(Long id, Long companyId, Long branchId) {
        VendorCustomer vc = getById(id, companyId, branchId);
        repository.delete(vc);

        logService.log("DELETE", "VendorCustomer", id,
                "Deleted Vendor/Customer with ID: " + id);
    }

    // ✅ Delete multiple (company/branch enforced)
    public void deleteMultiple(List<Long> ids, Long companyId, Long branchId) {
        List<VendorCustomer> customers = repository.findAllById(ids).stream()
                .filter(vc -> vc.getCompanyId().equals(companyId) && vc.getBranchId().equals(branchId))
                .toList();

        repository.deleteAll(customers);

        for (VendorCustomer vc : customers) {
            logService.log("DELETE", "VendorCustomer", vc.getId(),
                    "Deleted Vendor/Customer with ID: " + vc.getId());
        }
    }
}
