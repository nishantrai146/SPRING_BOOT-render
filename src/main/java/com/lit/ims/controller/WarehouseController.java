package com.lit.ims.controller;

import com.lit.ims.dto.WarehouseDTO;
import com.lit.ims.entity.Warehouse;
import com.lit.ims.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WarehouseController {

    private final WarehouseService warehouseService;

    // ✅ Create
    @PostMapping("/add")
    public ResponseEntity<?> addWarehouse(
            @RequestBody Warehouse warehouse,
            @RequestAttribute("companyId") Long companyId,
            @RequestAttribute("branchId") Long branchId
    ) {
        try {
            WarehouseDTO saved = warehouseService.saveWarehouse(warehouse, companyId, branchId);
            return ResponseEntity.ok(Map.of("warehouse", saved));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Update
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateWarehouse(
            @PathVariable Long id,
            @RequestBody Warehouse warehouse,
            @RequestAttribute("companyId") Long companyId,
            @RequestAttribute("branchId") Long branchId
    ) {
        try {
            WarehouseDTO updated = warehouseService.updateWarehouse(id, warehouse, companyId, branchId);
            return ResponseEntity.ok(Map.of("warehouse", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Get All
    @GetMapping
    public ResponseEntity<?> getAllWarehouses(
            @RequestAttribute("companyId") Long companyId,
            @RequestAttribute("branchId") Long branchId
    ) {
        List<WarehouseDTO> list = warehouseService.getAllWarehouses(companyId, branchId);
        return ResponseEntity.ok(Map.of("warehouses", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getWarehouseById(
            @PathVariable Long id,
            @RequestAttribute("companyId") Long companyId,
            @RequestAttribute("branchId") Long branchId
    ) {
        return warehouseService.getWarehouseById(id, companyId, branchId)
                .map(wh -> {
                    WarehouseDTO dto = WarehouseDTO.builder()
                            .id(wh.getId())
                            .trno(wh.getTrno())
                            .code(wh.getCode())
                            .name(wh.getName())
                            .status(wh.getStatus())
                            .build();

                    Map<String, Object> response = Map.of("warehouse", dto);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, Object> error = Map.of("error", "Warehouse not found");
                    return ResponseEntity.status(404).body(error);
                });
    }


    // ✅ Delete by ID
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteWarehouse(
            @PathVariable Long id,
            @RequestAttribute("companyId") Long companyId,
            @RequestAttribute("branchId") Long branchId
    ) {
        boolean deleted = warehouseService.deleteWarehouse(id, companyId, branchId);
        return deleted
                ? ResponseEntity.ok(Map.of("message", "Warehouse deleted successfully"))
                : ResponseEntity.status(404).body(Map.of("error", "Warehouse not found"));
    }

    // ✅ Delete Multiple
    @PostMapping("/delete-multiple")
    public ResponseEntity<?> deleteMultiple(
            @RequestBody List<Long> ids,
            @RequestAttribute("companyId") Long companyId,
            @RequestAttribute("branchId") Long branchId
    ) {
        warehouseService.deleteMultipleWarehouses(ids, companyId, branchId);
        return ResponseEntity.ok(Map.of("message", "Selected warehouses deleted successfully"));
    }
}
