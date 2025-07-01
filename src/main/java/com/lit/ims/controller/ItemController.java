package com.lit.ims.controller;

import com.lit.ims.dto.ItemDTO;
import com.lit.ims.entity.Item;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // ✅ Create Item
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Item>> addItem(@RequestBody ItemDTO request,
                                                     @RequestAttribute("companyId") Long companyId,
                                                     @RequestAttribute("branchId") Long branchId) {
        try {
            Item saved = itemService.saveItem(request, companyId, branchId);
            return ResponseEntity.ok(ApiResponse.<Item>builder()
                    .status(true)
                    .message("Item Added Successfully")
                    .data(saved)
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<Item>builder()
                    .status(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    // ✅ Get All Items
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Item>>> getAllItems(@RequestAttribute("companyId") Long companyId,
                                                               @RequestAttribute("branchId") Long branchId) {
        List<Item> items = itemService.getAllItems(companyId, branchId);
        return ResponseEntity.ok(ApiResponse.<List<Item>>builder()
                .status(true)
                .message("Items fetched successfully")
                .data(items)
                .build());
    }

    // ✅ Get Item by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Item>> getItemById(@PathVariable Long id,
                                                         @RequestAttribute("companyId") Long companyId,
                                                         @RequestAttribute("branchId") Long branchId) {
        return itemService.getItemById(id, companyId, branchId)
                .map(item -> ResponseEntity.ok(ApiResponse.<Item>builder()
                        .status(true)
                        .message("Item fetched successfully")
                        .data(item)
                        .build()))
                .orElse(ResponseEntity.badRequest().body(ApiResponse.<Item>builder()
                        .status(false)
                        .message("Item not found")
                        .build()));
    }

    // ✅ Update Item
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<Item>> updateItem(@PathVariable Long id,
                                                        @RequestBody ItemDTO dto,
                                                        @RequestAttribute("companyId") Long companyId,
                                                        @RequestAttribute("branchId") Long branchId) {
        try {
            Optional<Item> updated = itemService.updateItem(id, dto, companyId, branchId);

            if (updated.isPresent()) {
                return ResponseEntity.ok(ApiResponse.<Item>builder()
                        .status(true)
                        .message("Item updated successfully")
                        .data(updated.get())
                        .build());
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.<Item>builder()
                        .status(false)
                        .message("Item not found")
                        .build());
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<Item>builder()
                    .status(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    // ✅ Delete Single Item
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteItem(@PathVariable Long id,
                                                          @RequestAttribute("companyId") Long companyId,
                                                          @RequestAttribute("branchId") Long branchId) {
        try {
            boolean deleted = itemService.deleteItem(id, companyId, branchId);
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.<String>builder()
                        .status(true)
                        .message("Item deleted successfully")
                        .data("Deleted ID: " + id)
                        .build());
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                        .status(false)
                        .message("Item not found")
                        .build());
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .status(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    // ✅ Delete Multiple Items
    @DeleteMapping("/delete-multiple")
    public ResponseEntity<ApiResponse<String>> deleteMultipleItems(@RequestBody List<Long> ids,
                                                                   @RequestAttribute("companyId") Long companyId,
                                                                   @RequestAttribute("branchId") Long branchId) {
        try {
            itemService.deleteMultipleItems(ids, companyId, branchId);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .status(true)
                    .message("Items deleted successfully")
                    .data("Deleted IDs: " + ids)
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .status(false)
                    .message(e.getMessage())
                    .build());
        }
    }
}
