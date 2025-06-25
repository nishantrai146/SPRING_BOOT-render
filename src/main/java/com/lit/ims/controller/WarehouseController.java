package com.lit.ims.controller;

import com.lit.ims.dto.WarehouseDTO;
import com.lit.ims.entity.Warehouse;
import com.lit.ims.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping("/save")
    public ResponseEntity<?> saveWarehouse(@RequestBody Warehouse warehouse) {
        try {
            WarehouseDTO response = warehouseService.saveWarehouse(warehouse);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateWarehouse(@PathVariable Long id, @RequestBody Warehouse updated) {
        try {
            WarehouseDTO response = warehouseService.updateWarehouse(id, updated);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<WarehouseDTO>> getAll() {
        return ResponseEntity.ok(warehouseService.getAllWarehouses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return warehouseService.getWarehouseById(id)
                .<ResponseEntity<?>>map(wh -> ResponseEntity.ok(warehouseService.getAllWarehouses()
                        .stream()
                        .filter(dto -> dto.getCode().equals(wh.getCode()))
                        .findFirst()
                        .orElse(null)))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Warehouse not found")));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        boolean deleted = warehouseService.deleteWarehouse(id);
        return deleted
                ? ResponseEntity.ok(Map.of("message", "Deleted successfully"))
                : ResponseEntity.status(404).body(Map.of("error", "Warehouse not found"));
    }

    @PostMapping("/delete-multiple")
    public ResponseEntity<?> deleteMultiple(@RequestBody List<Long> ids) {
        warehouseService.deleteMultipleWarehouses(ids);
        return ResponseEntity.ok(Map.of("message", "Selected warehouses deleted"));
    }
}