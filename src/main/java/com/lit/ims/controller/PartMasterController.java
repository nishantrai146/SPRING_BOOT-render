package com.lit.ims.controller;

import com.lit.ims.dto.PartMasterDT0;
import com.lit.ims.service.PartMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/part")
@RequiredArgsConstructor
public class PartMasterController {

    private final PartMasterService partMasterService;

    @PostMapping("/save")
    public ResponseEntity<String> createPart(@RequestBody PartMasterDT0 pmd){
        partMasterService.savePart(pmd);
        return ResponseEntity.ok("Part Added Successfully");
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updatePart(@PathVariable Long id,@RequestBody PartMasterDT0 dto){
        partMasterService.updatePart(id,dto);
        return ResponseEntity.ok("Updated Successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartMasterDT0> getPart(@PathVariable Long id){
        return ResponseEntity.ok(partMasterService.getPartById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<PartMasterDT0>> getParts(){
        return ResponseEntity.ok(partMasterService.getAllParts());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deletePart(@PathVariable Long id){
        partMasterService.deletePart(id);
        return ResponseEntity.ok("Deleted Successfully");

    }

    @DeleteMapping("/dellete-multiple")
    public ResponseEntity<String > deleteMultiplePart(@RequestBody List<Long> ids){
        partMasterService.deleteMultiple(ids);
        return ResponseEntity.ok("Deleted Successfully");
    }


}
