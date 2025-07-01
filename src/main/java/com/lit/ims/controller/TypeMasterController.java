package com.lit.ims.controller;

import com.lit.ims.dto.TypeMasterDTO;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.TransactionLogService;
import com.lit.ims.service.TypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/type")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TypeMasterController {

    private final TypeService service;
    private final TransactionLogService logService;

    // ✅ Save
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<TypeMasterDTO>> save(@RequestBody TypeMasterDTO dto,
                                                           @RequestAttribute("companyId") Long companyId,
                                                           @RequestAttribute("branchId") Long branchId) {
        TypeMasterDTO saved = service.save(dto, companyId, branchId);

        logService.log("CREATE", "TypeMaster", saved.getId(),
                "Type created with TRNO: " + saved.getTrno());

        return ResponseEntity.ok(new ApiResponse<>(true, "Type created successfully", saved));
    }

    // ✅ Update
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<TypeMasterDTO>> update(@PathVariable Long id,
                                                             @RequestBody TypeMasterDTO dto,
                                                             @RequestAttribute("companyId") Long companyId,
                                                             @RequestAttribute("branchId") Long branchId) {
        TypeMasterDTO updated = service.update(id, dto, companyId, branchId);

        logService.log("UPDATE", "TypeMaster", id,
                "Type updated to name: " + updated.getName());

        return ResponseEntity.ok(new ApiResponse<>(true, "Type updated successfully", updated));
    }

    // ✅ Get One
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TypeMasterDTO>> getOne(@PathVariable Long id) {
        TypeMasterDTO type = service.getOne(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Type fetched successfully", type));
    }

    // ✅ Get All
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<TypeMasterDTO>>> getAll(@RequestAttribute("companyId") Long companyId,
                                                                   @RequestAttribute("branchId") Long branchId) {
        List<TypeMasterDTO> list = service.getAll(companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Types fetched successfully", list));
    }

    // ✅ Delete One
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        String trno = service.getTrnoById(id);
        service.delete(id);

        logService.log("DELETE", "TypeMaster", id,
                "Type deleted with TRNO: " + trno);

        return ResponseEntity.ok(new ApiResponse<>(true, "Type deleted successfully", null));
    }

    // ✅ Delete Multiple
    @PostMapping("/delete-multiple")
    public ResponseEntity<ApiResponse<String>> deleteMultiple(@RequestBody List<Long> ids) {
        List<String> trnos = service.getTrnosByIds(ids);
        service.deleteMultiple(ids);

        logService.log("DELETE", "TypeMaster", null,
                "Types deleted with TRNOs: " + trnos);

        return ResponseEntity.ok(new ApiResponse<>(true, "Types deleted successfully", null));
    }
}
