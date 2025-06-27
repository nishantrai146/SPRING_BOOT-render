package com.lit.ims.controller;

import com.lit.ims.dto.VendorCustomerDTO;
import com.lit.ims.entity.VendorCustomer;
import com.lit.ims.service.VendorCustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vendor-customer")
public class VendorCustomerController {

    @Autowired
    private VendorCustomerService service;

    // ✅ Create
    @PostMapping("/add")
    public ResponseEntity<VendorCustomer> add(@Valid @RequestBody VendorCustomerDTO dto,
                                              @RequestAttribute("companyId") Long companyId,
                                              @RequestAttribute("branchId") Long branchId) {
        VendorCustomer saved = service.add(dto, companyId, branchId);
        return ResponseEntity.ok(saved);
    }

    // ✅ Get All for company & branch
    @GetMapping("/all")
    public ResponseEntity<Map<String,Object>> getAll(@RequestAttribute("companyId") Long companyId,
                                                       @RequestAttribute("branchId") Long branchId) {
        List<VendorCustomer> list = service.getAll(companyId, branchId);

        Map<String,Object> response=new HashMap<>();
        response.put("businessPartner",list);
        return ResponseEntity.ok(response);
    }

    // ✅ Get by ID
    @GetMapping("/get/{id}")
    public ResponseEntity<VendorCustomer> getById(@PathVariable Long id,
                                                  @RequestAttribute("companyId") Long companyId,
                                                  @RequestAttribute("branchId") Long branchId) {
        VendorCustomer vc = service.getById(id, companyId, branchId);
        return ResponseEntity.ok(vc);
    }

    // ✅ Update
    @PutMapping("/update/{id}")
    public ResponseEntity<VendorCustomer> update(@PathVariable Long id,
                                                 @Valid @RequestBody VendorCustomerDTO dto,
                                                 @RequestAttribute("companyId") Long companyId,
                                                 @RequestAttribute("branchId") Long branchId) {
        VendorCustomer updated = service.update(id, dto, companyId, branchId);
        return ResponseEntity.ok(updated);
    }

    // ✅ Delete
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @RequestAttribute("companyId") Long companyId,
                                    @RequestAttribute("branchId") Long branchId) {
        service.delete(id, companyId, branchId);
        return ResponseEntity.ok("Deleted successfully");
    }

    // ✅ Delete Multiple
    @DeleteMapping("/delete-multiple")
    public ResponseEntity<?> deleteMultiple(@RequestBody List<Long> ids,
                                            @RequestAttribute("companyId") Long companyId,
                                            @RequestAttribute("branchId") Long branchId) {
        service.deleteMultiple(ids, companyId, branchId);
        return ResponseEntity.ok("Deleted successfully");
    }
}
