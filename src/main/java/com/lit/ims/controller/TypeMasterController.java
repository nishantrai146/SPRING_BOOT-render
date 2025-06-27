package com.lit.ims.controller;

import com.lit.ims.dto.TypeMasterDTO;
import com.lit.ims.service.TransactionLogService;
import com.lit.ims.service.TypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/type")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TypeMasterController {

    private final TypeService service;
    private final TransactionLogService logService;

    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody TypeMasterDTO dto,
                                  @RequestAttribute("companyId") Long companyId,
                                  @RequestAttribute("branchId") Long branchId) {
        try {
            TypeMasterDTO saved = service.save(dto, companyId, branchId);

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

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody TypeMasterDTO dto,
                                    @RequestAttribute("companyId") Long companyId,
                                    @RequestAttribute("branchId") Long branchId) {
        TypeMasterDTO updated = service.update(id, dto, companyId, branchId);

        logService.log(
                "UPDATE",
                "TypeMaster",
                id,
                "Type updated to name: " + updated.getName()
        );

        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TypeMasterDTO> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOne(id));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll(@RequestAttribute("companyId") Long companyId,
                                    @RequestAttribute("branchId") Long branchId) {
        List<TypeMasterDTO> list = service.getAll(companyId, branchId);

        return ResponseEntity.ok(
                Map.of("types", list)  // ✅ Key name 'types'
        );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        String trno = service.getTrnoById(id);
        service.delete(id);

        logService.log(
                "DELETE",
                "TypeMaster",
                id,
                "Type deleted with TRNO: " + trno
        );

        return ResponseEntity.ok("Deleted Successfully");
    }

    @PostMapping("/delete-multiple")
    public ResponseEntity<String> deleteMultiple(@RequestBody List<Long> ids) {
        List<String> trnos = service.getTrnosByIds(ids);
        service.deleteMultiple(ids);

        logService.log(
                "DELETE",
                "TypeMaster",
                null,
                "Types deleted with TRNOs: " + trnos
        );

        return ResponseEntity.ok("Deleted Successfully");
    }
}
