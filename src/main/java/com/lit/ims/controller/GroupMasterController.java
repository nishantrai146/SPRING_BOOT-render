package com.lit.ims.controller;

import com.lit.ims.dto.GroupMasterDTO;
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
    public ResponseEntity<?> save(@RequestBody GroupMasterDTO dto) {
        try {
            GroupMasterDTO saved = service.save(dto);

            logService.log(
                    "CREATE",
                    "GroupMaster",
                    saved.getId(),
                    "Group created with TRNO: " + saved.getTrno()
            );

            return ResponseEntity.ok(saved);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // ✅ Update
    @PutMapping("/update/{id}")
    public ResponseEntity<GroupMasterDTO> update(@PathVariable Long id, @RequestBody GroupMasterDTO dto) {
        GroupMasterDTO updated = service.update(id, dto);

        logService.log(
                "UPDATE",
                "GroupMaster",
                id,
                "Group updated to name: " + updated.getName()
        );

        return ResponseEntity.ok(updated);
    }

    // ✅ Get one
    @GetMapping("/{id}")
    public ResponseEntity<GroupMasterDTO> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOne(id));
    }

    // ✅ Get all
    @GetMapping("/all")
    public ResponseEntity<List<GroupMasterDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // ✅ Delete one
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.delete(id);

        logService.log(
                "DELETE",
                "GroupMaster",
                id,
                "Group deleted"
        );

        return ResponseEntity.ok("Deleted Successfully");
    }

    // ✅ Delete multiple
    @DeleteMapping("/delete-multiple")
    public ResponseEntity<String> delete(@RequestBody List<Long> ids) {
        service.deleteMultiple(ids);

        logService.log(
                "DELETE",
                "GroupMaster",
                null, // Multiple IDs — optionally store as null or as CSV in details
                "Groups deleted with IDs: " + ids
        );

        return ResponseEntity.ok("Deleted Successfully");
    }
}
