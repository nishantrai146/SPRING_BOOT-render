package com.lit.ims.controller;

import com.lit.ims.dto.VendorCustomerDTO;
import com.lit.ims.entity.VendorCustomer;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.VendorCustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendor-customer")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VendorCustomerController {

    private final VendorCustomerService service;

    // ✅ Create
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<VendorCustomer>> add(@Valid @RequestBody VendorCustomerDTO dto,
                                                           @RequestAttribute("companyId") Long companyId,
                                                           @RequestAttribute("branchId") Long branchId) {
        VendorCustomer saved = service.add(dto, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Business Partner added successfully", saved));
    }

    // ✅ Get All
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<VendorCustomer>>> getAll(@RequestAttribute("companyId") Long companyId,
                                                                    @RequestAttribute("branchId") Long branchId) {
        List<VendorCustomer> list = service.getAll(companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched successfully", list));
    }

    // ✅ Get by ID
    @GetMapping("/get/{id}")
    public ResponseEntity<ApiResponse<VendorCustomer>> getById(@PathVariable Long id,
                                                               @RequestAttribute("companyId") Long companyId,
                                                               @RequestAttribute("branchId") Long branchId) {
        VendorCustomer vc = service.getById(id, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched successfully", vc));
    }

    // ✅ Update
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<VendorCustomer>> update(@PathVariable Long id,
                                                              @Valid @RequestBody VendorCustomerDTO dto,
                                                              @RequestAttribute("companyId") Long companyId,
                                                              @RequestAttribute("branchId") Long branchId) {
        VendorCustomer updated = service.update(id, dto, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Updated successfully", updated));
    }

    // ✅ Delete
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id,
                                                      @RequestAttribute("companyId") Long companyId,
                                                      @RequestAttribute("branchId") Long branchId) {
        service.delete(id, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Deleted successfully", null));
    }

    // ✅ Delete Multiple
    @DeleteMapping("/delete-multiple")
    public ResponseEntity<ApiResponse<String>> deleteMultiple(@RequestBody List<Long> ids,
                                                              @RequestAttribute("companyId") Long companyId,
                                                              @RequestAttribute("branchId") Long branchId) {
        service.deleteMultiple(ids, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Deleted successfully", null));
    }
    // ✅ Get All Vendors
    @GetMapping("/vendors")
    public ResponseEntity<ApiResponse<List<VendorCustomer>>> getAllVendors(
            @RequestAttribute("companyId") Long companyId,
            @RequestAttribute("branchId") Long branchId
    ) {
        List<VendorCustomer> vendors = service.getAllVendors(companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Vendors fetched successfully", vendors));
    }

}
