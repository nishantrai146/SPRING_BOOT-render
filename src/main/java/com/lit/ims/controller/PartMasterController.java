package com.lit.ims.controller;

import com.lit.ims.dto.PartMasterDTO;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.PartMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/part")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PartMasterController {

    private final PartMasterService partMasterService;

    // ✅ Create
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<PartMasterDTO>> createPart(@RequestBody PartMasterDTO dto,
                                                                 @RequestAttribute("companyId") Long companyId,
                                                                 @RequestAttribute("branchId") Long branchId) {
        PartMasterDTO saved = partMasterService.savePart(dto, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Part created successfully", saved));
    }

    // ✅ Update
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<PartMasterDTO>> updatePart(@PathVariable Long id,
                                                                 @RequestBody PartMasterDTO dto,
                                                                 @RequestAttribute("companyId") Long companyId,
                                                                 @RequestAttribute("branchId") Long branchId) {
        PartMasterDTO updated = partMasterService.updatePart(id, dto, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Part updated successfully", updated));
    }

    // ✅ Get One
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PartMasterDTO>> getPart(@PathVariable Long id,
                                                              @RequestAttribute("companyId") Long companyId,
                                                              @RequestAttribute("branchId") Long branchId) {
        PartMasterDTO part = partMasterService.getPartById(id, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Part fetched successfully", part));
    }

    // ✅ Get All
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<PartMasterDTO>>> getParts(@RequestAttribute("companyId") Long companyId,
                                                                     @RequestAttribute("branchId") Long branchId) {
        List<PartMasterDTO> list = partMasterService.getAllParts(companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Parts fetched successfully", list));
    }

    // ✅ Delete One
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deletePart(@PathVariable Long id,
                                                          @RequestAttribute("companyId") Long companyId,
                                                          @RequestAttribute("branchId") Long branchId) {
        partMasterService.deletePart(id, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Part deleted successfully", null));
    }

    // ✅ Delete Multiple
    @DeleteMapping("/delete-multiple")
    public ResponseEntity<ApiResponse<String>> deleteMultiplePart(@RequestBody List<Long> ids,
                                                                  @RequestAttribute("companyId") Long companyId,
                                                                  @RequestAttribute("branchId") Long branchId) {
        partMasterService.deleteMultiple(ids, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Parts deleted successfully", null));
    }
}
