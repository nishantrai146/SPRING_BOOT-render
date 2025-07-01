package com.lit.ims.controller;

import com.lit.ims.dto.BomDTO;
import com.lit.ims.entity.BOM;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.BomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bom")
@RequiredArgsConstructor
public class BomController {

    private final BomService bomService;

    @PostMapping("/add")
    public ResponseEntity<?> addBom(@RequestBody BomDTO dto,
                                    @RequestAttribute("companyId") Long companyId,
                                    @RequestAttribute("branchId") Long branchId) {
        BOM bom = bomService.saveBom(dto, companyId, branchId);
        return ResponseEntity.ok(ApiResponse.builder()
                .status(true)
                .message("BOM added successfully")
                .data(bom)
                .build());
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll(@RequestAttribute("companyId") Long companyId,
                                    @RequestAttribute("branchId") Long branchId) {
        List<BomDTO> boms = bomService.getAll(companyId, branchId);
        return ResponseEntity.ok(ApiResponse.builder()
                .status(true)
                .message("BOM list fetched successfully")
                .data(boms)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return bomService.getById(id)
                .map(bom -> ResponseEntity.ok(ApiResponse.builder()
                        .status(true)
                        .message("BOM fetched successfully")
                        .data(bom)
                        .build()))
                .orElse(ResponseEntity.status(404).body(ApiResponse.builder()
                        .status(false)
                        .message("BOM not found")
                        .data(null)
                        .build()));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateBom(@PathVariable Long id,
                                       @RequestBody BomDTO dto) {
        BOM updated = bomService.updateBom(id, dto);
        return ResponseEntity.ok(ApiResponse.builder()
                .status(true)
                .message("BOM updated successfully")
                .data(updated.getId())
                .build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteBom(@PathVariable Long id) {
        bomService.deleteBom(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .status(true)
                .message("BOM deleted successfully")
                .data(null)
                .build());
    }

    @PostMapping("/delete-multiple")
    public ResponseEntity<?> deleteMultiple(@RequestBody List<Long> ids) {
        bomService.deleteMultiple(ids);
        return ResponseEntity.ok(ApiResponse.builder()
                .status(true)
                .message("BOMs deleted successfully")
                .data(null)
                .build());
    }
}
