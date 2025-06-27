package com.lit.ims.controller;

import com.lit.ims.dto.PartMasterDTO;
import com.lit.ims.service.PartMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/part")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PartMasterController {

    private final PartMasterService partMasterService;

    // ✅ Create
    @PostMapping("/save")
    public ResponseEntity<?> createPart(@RequestBody PartMasterDTO dto,
                                        @RequestAttribute("companyId") Long companyId,
                                        @RequestAttribute("branchId") Long branchId) {
        PartMasterDTO saved = partMasterService.savePart(dto, companyId, branchId);
        return ResponseEntity.ok(saved);
    }

    // ✅ Update
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePart(@PathVariable Long id,
                                        @RequestBody PartMasterDTO dto,
                                        @RequestAttribute("companyId") Long companyId,
                                        @RequestAttribute("branchId") Long branchId) {
        PartMasterDTO updated = partMasterService.updatePart(id, dto, companyId, branchId);
        return ResponseEntity.ok(updated);
    }

    // ✅ Get One
    @GetMapping("/{id}")
    public ResponseEntity<?> getPart(@PathVariable Long id,
                                     @RequestAttribute("companyId") Long companyId,
                                     @RequestAttribute("branchId") Long branchId) {
        PartMasterDTO part = partMasterService.getPartById(id, companyId, branchId);
        return ResponseEntity.ok(part);
    }

    // ✅ Get All
    @GetMapping("/all")
    public ResponseEntity<?> getParts(@RequestAttribute("companyId") Long companyId,
                                      @RequestAttribute("branchId") Long branchId) {
        List<PartMasterDTO> list = partMasterService.getAllParts(companyId, branchId);
        return ResponseEntity.ok(Map.of("parts", list));
    }

    // ✅ Delete One
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deletePart(@PathVariable Long id,
                                             @RequestAttribute("companyId") Long companyId,
                                             @RequestAttribute("branchId") Long branchId) {
        partMasterService.deletePart(id, companyId, branchId);
        return ResponseEntity.ok("Deleted Successfully");
    }

    // ✅ Delete Multiple
    @DeleteMapping("/delete-multiple")
    public ResponseEntity<String> deleteMultiplePart(@RequestBody List<Long> ids,
                                                     @RequestAttribute("companyId") Long companyId,
                                                     @RequestAttribute("branchId") Long branchId) {
        partMasterService.deleteMultiple(ids, companyId, branchId);
        return ResponseEntity.ok("Deleted Successfully");
    }
}
