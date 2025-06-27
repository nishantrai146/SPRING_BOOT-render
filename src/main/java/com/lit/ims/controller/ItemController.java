package com.lit.ims.controller;

import com.lit.ims.dto.ItemDTO;
import com.lit.ims.entity.Item;
import com.lit.ims.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // ✅ Create Item
    @PostMapping("/add")
    public ResponseEntity<?> addItem(@RequestBody ItemDTO request,
                                     @RequestAttribute("companyId") Long companyId,
                                     @RequestAttribute("branchId") Long branchId) {
        try {
            Item saved = itemService.saveItem(request, companyId, branchId);
            return ResponseEntity.ok(Map.of(
                    "message", "Item Added Successfully",
                    "item", saved
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
    // ✅ Get All Items
    @GetMapping("/all")
    public ResponseEntity<?> getAllItems(@RequestAttribute("companyId") Long companyId,
                                         @RequestAttribute("branchId") Long branchId) {
        List<Item> items = itemService.getAllItems(companyId, branchId);
        return ResponseEntity.ok(Map.of("items", items));
    }


    // ✅ Get Item by ID
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id,
                                            @RequestAttribute("companyId") Long companyId,
                                            @RequestAttribute("branchId") Long branchId) {
        return itemService.getItemById(id, companyId, branchId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



    // ✅ Update Item
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateItem(@PathVariable Long id,
                                             @RequestBody ItemDTO dto,
                                             @RequestAttribute("companyId") Long companyId,
                                             @RequestAttribute("branchId") Long branchId) {
        Optional<Item> updated = itemService.updateItem(id, dto, companyId, branchId);

        if (updated.isPresent()) {
            return ResponseEntity.ok("Item Updated Successfully");
        } else {
            return ResponseEntity.badRequest().body("Item Not Found");
        }
    }

    // ✅ Delete Single Item
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id,
                                        @RequestAttribute("companyId") Long companyId,
                                        @RequestAttribute("branchId") Long branchId) {
        boolean deleted = itemService.deleteItem(id, companyId, branchId);
        if (deleted) {
            return ResponseEntity.ok("Item Deleted Successfully");
        } else {
            return ResponseEntity.badRequest().body("Item Not Found");
        }
    }

    // ✅ Delete Multiple Items
    @DeleteMapping("/delete-multiple")
    public ResponseEntity<?> deleteMultipleItems(@RequestBody List<Long> ids,
                                                 @RequestAttribute("companyId") Long companyId,
                                                 @RequestAttribute("branchId") Long branchId) {
        itemService.deleteMultipleItems(ids, companyId, branchId);
        return ResponseEntity.ok("Items Deleted Successfully");
    }
}
