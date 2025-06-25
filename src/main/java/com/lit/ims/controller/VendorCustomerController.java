package com.lit.ims.controller;


import com.lit.ims.dto.VendorCustomerDTO;
import com.lit.ims.entity.VendorCustomer;
import com.lit.ims.service.VendorCustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendor-customer")
public class VendorCustomerController {
    @Autowired
    private VendorCustomerService vcs;
    @PostMapping("/add")
    public ResponseEntity<VendorCustomer> add(@Valid @RequestBody VendorCustomerDTO dto){
        return ResponseEntity.ok(vcs.add(dto));
    }

    @GetMapping("/all")
    public ResponseEntity<List<VendorCustomer>> getAll(){
        return ResponseEntity.ok(vcs.getAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<VendorCustomer> getById(@PathVariable Long id){
        return ResponseEntity.ok(vcs.getById(id));
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<VendorCustomer> update(@PathVariable Long id, @RequestBody VendorCustomerDTO dto) {
        return ResponseEntity.ok(vcs.update(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vcs.delete(id);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/delete-multiple")
    public ResponseEntity<Void> DeleteMultiple(@RequestBody List<Long> ids) {
        vcs.deleteMultiple(ids);
        return ResponseEntity.noContent().build();
    }
}
