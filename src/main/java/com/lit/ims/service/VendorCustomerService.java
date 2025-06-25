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

    public VendorCustomer add(VendorCustomerDTO dto) {
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

        VendorCustomer saved = repository.save(vc);

        // ✅ Log creation
        logService.log("CREATE", "VendorCustomer", saved.getId(), "Created " + saved.getType() + ": " + saved.getName());

        return saved;
    }

    public List<VendorCustomer> getAll() {
        return repository.findAll();
    }

    public VendorCustomer getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public VendorCustomer update(Long id, VendorCustomerDTO dto) {
        VendorCustomer vc = repository.findById(id).orElseThrow();
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

        // ✅ Log update
        logService.log("UPDATE", "VendorCustomer", updated.getId(), "Updated " + updated.getType() + ": " + updated.getName());

        return updated;
    }

    public void delete(Long id) {
        repository.deleteById(id);

        // ✅ Log deletion
        logService.log("DELETE", "VendorCustomer", id, "Deleted Vendor/Customer with ID: " + id);
    }

    public void deleteMultiple(List<Long> ids) {
        repository.deleteAllById(ids);

        // ✅ Log each deletion
        for (Long id : ids) {
            logService.log("DELETE", "VendorCustomer", id, "Deleted Vendor/Customer with ID: " + id);
        }
    }
}
