package com.lit.ims.controller;

import com.lit.ims.dto.GroupMasterDTO;
import com.lit.ims.service.GroupMasterService;
import com.lit.ims.service.TransactionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GroupMasterController {

    private final GroupMasterService service;
    private final TransactionLogService logService;

    // ✅ Save
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody GroupMasterDTO dto,
                                  @RequestAttribute("companyId") Long companyId,
                                  @RequestAttribute("branchId") Long branchId) {
        GroupMasterDTO saved = service.save(dto, companyId, branchId);

        logService.log("CREATE", "GroupMaster", saved.getId(),
                "Group created with TRNO: " + saved.getTrno());

        return ResponseEntity.ok(saved);
    }

    // ✅ Update
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody GroupMasterDTO dto,
                                    @RequestAttribute("companyId") Long companyId,
                                    @RequestAttribute("branchId") Long branchId) {
        GroupMasterDTO updated = service.update(id, dto, companyId, branchId);

        logService.log("UPDATE", "GroupMaster", id,
                "Group updated to name: " + updated.getName());

        return ResponseEntity.ok(updated);
    }

    // ✅ Get One
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id,
                                    @RequestAttribute("companyId") Long companyId,
                                    @RequestAttribute("branchId") Long branchId) {
        return ResponseEntity.ok(service.getOne(id, companyId, branchId));
    }

    // ✅ Get All
    @GetMapping("/all")
    public ResponseEntity<?> getAll(@RequestAttribute("companyId") Long companyId,
                                    @RequestAttribute("branchId") Long branchId) {
        List<GroupMasterDTO> list = service.getAll(companyId, branchId);
        return ResponseEntity.ok(Map.of("groups", list));
    }

    // ✅ Delete
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @RequestAttribute("companyId") Long companyId,
                                    @RequestAttribute("branchId") Long branchId) {
        service.delete(id, companyId, branchId);

        logService.log("DELETE", "GroupMaster", id, "Group deleted");

        return ResponseEntity.ok("Deleted Successfully");
    }

    // ✅ Delete Multiple
    @DeleteMapping("/delete-multiple")
    public ResponseEntity<?> deleteMultiple(@RequestBody List<Long> ids,
                                            @RequestAttribute("companyId") Long companyId,
                                            @RequestAttribute("branchId") Long branchId) {
        service.deleteMultiple(ids, companyId, branchId);

        logService.log("DELETE", "GroupMaster", null,
                "Groups deleted with IDs: " + ids);

        return ResponseEntity.ok("Deleted Successfully");
    }
}
