package com.lit.ims.controller;



import com.lit.ims.dto.TypeMasterDTO;
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
    public ResponseEntity<?> save(@RequestBody TypeMasterDTO dto) {
        try {
            TypeMasterDTO saved = service.save(dto);

            logService.log(
                    "CREATE",
                    "TypeMaster",
                    saved.getId(),
                    "Type created with TRNO: " + saved.getTrno()
            );

            return ResponseEntity.ok(saved);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // ✅ Update
    @PutMapping("/update/{id}")
    public ResponseEntity<TypeMasterDTO> update(@PathVariable Long id, @RequestBody TypeMasterDTO dto) {
        TypeMasterDTO updated = service.update(id, dto);

        logService.log(
                "UPDATE",
                "TypeMaster",
                id,
                "Type updated to name: " + updated.getName()
        );

        return ResponseEntity.ok(updated);
    }

    // ✅ Get one
    @GetMapping("/{id}")
    public ResponseEntity<TypeMasterDTO> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOne(id));
    }

    // ✅ Get all
    @GetMapping("/all")
    public ResponseEntity<List<TypeMasterDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // ✅ Delete one
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        String trno = service.getTrnoById(id);
        service.delete(id);

        logService.log(
                "DELETE",
                "TypeMaster",
                id,
                "Type deleted with trno: "+trno
        );

        return ResponseEntity.ok("Deleted Successfully");
    }

    // ✅ Delete multiple
    @DeleteMapping("/delete-multiple")
    public ResponseEntity<String> delete(@RequestBody List<Long> ids) {
        List<String> trnos=service.getTrnosByIds(ids);
        service.deleteMultiple(ids);

        logService.log(
                "DELETE",
                "TypeMaster",
                null, // Multiple IDs — optionally store as null or as CSV in details
                "Type deleted with IDs: " + trnos
        );

        return ResponseEntity.ok("Deleted Successfully");
    }
}
