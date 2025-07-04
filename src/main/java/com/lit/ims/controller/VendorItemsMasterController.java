package com.lit.ims.controller;

import com.lit.ims.dto.VendorItemsMasterDTO;
import com.lit.ims.entity.Item;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.VendorItemsMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendor-item")
@RequiredArgsConstructor
public class VendorItemsMasterController {

    private final VendorItemsMasterService service;

    // ✅ Create
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<VendorItemsMasterDTO>> create(@RequestBody VendorItemsMasterDTO dto,
                                                                    @RequestAttribute("companyId") Long companyId,
                                                                    @RequestAttribute("branchId") Long branchId) {
        VendorItemsMasterDTO saved = service.save(dto, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Vendor Item created successfully", saved));
    }

    // ✅ Update
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<VendorItemsMasterDTO>> update(@PathVariable Long id,
                                                                    @RequestBody VendorItemsMasterDTO dto,
                                                                    @RequestAttribute("companyId") Long companyId,
                                                                    @RequestAttribute("branchId") Long branchId) {
        VendorItemsMasterDTO updated = service.update(id, dto, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Vendor Item updated successfully", updated));
    }

    // ✅ Get One
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VendorItemsMasterDTO>> getById(@PathVariable Long id,
                                                                     @RequestAttribute("companyId") Long companyId,
                                                                     @RequestAttribute("branchId") Long branchId) {
        VendorItemsMasterDTO dto = service.getById(id, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Vendor Item fetched successfully", dto));
    }

    // ✅ Get All
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<VendorItemsMasterDTO>>> getAll(@RequestAttribute("companyId") Long companyId,
                                                                          @RequestAttribute("branchId") Long branchId) {
        List<VendorItemsMasterDTO> list = service.getAll(companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Vendor Items fetched successfully", list));
    }

    // ✅ Delete One
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id,
                                                      @RequestAttribute("companyId") Long companyId,
                                                      @RequestAttribute("branchId") Long branchId) {
        service.delete(id, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Vendor Item deleted successfully", null));
    }

    // ✅ Delete Multiple
    @DeleteMapping("/delete-multiple")
    public ResponseEntity<ApiResponse<String>> deleteMultiple(@RequestBody List<Long> ids,
                                                              @RequestAttribute("companyId") Long companyId,
                                                              @RequestAttribute("branchId") Long branchId) {
        service.deleteMultiple(ids, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Vendor Items deleted successfully", null));
    }

    @GetMapping("/items/{vendorCode}")
    public ResponseEntity<ApiResponse<List<VendorItemsMasterDTO>>> getItemByVendorCode(
            @PathVariable String vendorCode,
            @RequestAttribute("companyId") Long companyId,
            @RequestAttribute("branchId") Long branchId) {
        List<VendorItemsMasterDTO> result=service.getItemByVendor(vendorCode,companyId,branchId);
        return ResponseEntity.ok(new ApiResponse<>(true,"Fetched Vendor Items",result));
    }
}
