package com.lit.ims.controller;

import com.lit.ims.dto.WarehouseDTO;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WarehouseController {

    private final WarehouseService warehouseService;

    // ✅ Create
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<WarehouseDTO>> addWarehouse(
            @RequestBody WarehouseDTO warehouseDTO,
            @RequestAttribute("companyId") Long companyId,
            @RequestAttribute("branchId") Long branchId
    ) {
        WarehouseDTO saved = warehouseService.saveWarehouse(warehouseDTO, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Warehouse added successfully", saved));
    }

    // ✅ Update
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<WarehouseDTO>> updateWarehouse(
            @PathVariable Long id,
            @RequestBody WarehouseDTO warehouseDTO,
            @RequestAttribute("companyId") Long companyId,
            @RequestAttribute("branchId") Long branchId
    ) {
        WarehouseDTO updated = warehouseService.updateWarehouse(id, warehouseDTO, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Warehouse updated successfully", updated));
    }

    // ✅ Get All
    @GetMapping
    public ResponseEntity<ApiResponse<List<WarehouseDTO>>> getAllWarehouses(
            @RequestAttribute("companyId") Long companyId,
            @RequestAttribute("branchId") Long branchId
    ) {
        List<WarehouseDTO> list = warehouseService.getAllWarehouses(companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched successfully", list));
    }

    // ✅ Get by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WarehouseDTO>> getWarehouseById(
            @PathVariable Long id,
            @RequestAttribute("companyId") Long companyId,
            @RequestAttribute("branchId") Long branchId
    ) {
        WarehouseDTO dto = warehouseService.getWarehouseById(id, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched successfully", dto));
    }

    // ✅ Delete by ID
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteWarehouse(
            @PathVariable Long id,
            @RequestAttribute("companyId") Long companyId,
            @RequestAttribute("branchId") Long branchId
    ) {
        warehouseService.deleteWarehouse(id, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Warehouse deleted successfully", null));
    }

    // ✅ Delete Multiple
    @PostMapping("/delete-multiple")
    public ResponseEntity<ApiResponse<String>> deleteMultiple(
            @RequestBody List<Long> ids,
            @RequestAttribute("companyId") Long companyId,
            @RequestAttribute("branchId") Long branchId
    ) {
        warehouseService.deleteMultipleWarehouses(ids, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Selected warehouses deleted successfully", null));
    }
}
