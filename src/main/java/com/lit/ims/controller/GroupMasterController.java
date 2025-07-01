package com.lit.ims.controller;

import com.lit.ims.dto.GroupMasterDTO;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.GroupMasterService;
import com.lit.ims.service.TransactionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GroupMasterController {

    private final GroupMasterService service;
    private final TransactionLogService logService;

    // ✅ Save
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<GroupMasterDTO>> save(@RequestBody GroupMasterDTO dto,
                                                            @RequestAttribute("companyId") Long companyId,
                                                            @RequestAttribute("branchId") Long branchId) {
        GroupMasterDTO saved = service.save(dto, companyId, branchId);

        logService.log("CREATE", "GroupMaster", saved.getId(),
                "Group created with TRNO: " + saved.getTrno());

        return ResponseEntity.ok(ApiResponse.<GroupMasterDTO>builder()
                .status(true)
                .message("Group created successfully")
                .data(saved)
                .build());
    }

    // ✅ Update
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<GroupMasterDTO>> update(@PathVariable Long id,
                                                              @RequestBody GroupMasterDTO dto,
                                                              @RequestAttribute("companyId") Long companyId,
                                                              @RequestAttribute("branchId") Long branchId) {
        GroupMasterDTO updated = service.update(id, dto, companyId, branchId);

        logService.log("UPDATE", "GroupMaster", id,
                "Group updated to name: " + updated.getName());

        return ResponseEntity.ok(ApiResponse.<GroupMasterDTO>builder()
                .status(true)
                .message("Group updated successfully")
                .data(updated)
                .build());
    }

    // ✅ Get One
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GroupMasterDTO>> getOne(@PathVariable Long id,
                                                              @RequestAttribute("companyId") Long companyId,
                                                              @RequestAttribute("branchId") Long branchId) {
        GroupMasterDTO data = service.getOne(id, companyId, branchId);

        return ResponseEntity.ok(ApiResponse.<GroupMasterDTO>builder()
                .status(true)
                .message("Group fetched successfully")
                .data(data)
                .build());
    }

    // ✅ Get All
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<GroupMasterDTO>>> getAll(@RequestAttribute("companyId") Long companyId,
                                                                    @RequestAttribute("branchId") Long branchId) {
        List<GroupMasterDTO> list = service.getAll(companyId, branchId);

        return ResponseEntity.ok(ApiResponse.<List<GroupMasterDTO>>builder()
                .status(true)
                .message("Groups fetched successfully")
                .data(list)
                .build());
    }

    // ✅ Delete
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id,
                                                      @RequestAttribute("companyId") Long companyId,
                                                      @RequestAttribute("branchId") Long branchId) {
        service.delete(id, companyId, branchId);

        logService.log("DELETE", "GroupMaster", id, "Group deleted");

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status(true)
                .message("Group deleted successfully")
                .data("Deleted Successfully")
                .build());
    }

    // ✅ Delete Multiple
    @DeleteMapping("/delete-multiple")
    public ResponseEntity<ApiResponse<String>> deleteMultiple(@RequestBody List<Long> ids,
                                                              @RequestAttribute("companyId") Long companyId,
                                                              @RequestAttribute("branchId") Long branchId) {
        service.deleteMultiple(ids, companyId, branchId);

        logService.log("DELETE", "GroupMaster", null,
                "Groups deleted with IDs: " + ids);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status(true)
                .message("Groups deleted successfully")
                .data("Deleted Successfully")
                .build());
    }
}
