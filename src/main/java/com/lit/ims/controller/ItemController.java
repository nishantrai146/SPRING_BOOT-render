package com.lit.ims.controller;

import com.lit.ims.dto.ItemDTO;
import com.lit.ims.entity.Item;
import com.lit.ims.service.ItemService;
import com.lit.ims.service.TransactionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private TransactionLogService logService;

    // ✅ Create Item
    @PostMapping("/add")
    public Item addItem(@RequestBody ItemDTO request) {
        Item saved = itemService.saveItem(request);

        logService.log(
                "CREATE",
                "Item",
                saved.getId(),
                "Item created with code: " + saved.getCode()
        );

        return saved;
    }

    // ✅ Get All Items
    @GetMapping("/all")
    public List<Item> getItem() {
        return itemService.getAllItem();
    }

    // ✅ Get Item by ID
    @GetMapping("/{id}")
    public Optional<Item> getItemById(@PathVariable Long id) {
        return itemService.getItemById(id);
    }

    // ✅ Update Item
    @PutMapping("/update/{id}")
    public Optional<Item> updateItem(@PathVariable Long id, @RequestBody ItemDTO dto) {
        Optional<Item> updated = itemService.updateItem(id, dto);

        updated.ifPresent(item -> logService.log(
                "UPDATE",
                "Item",
                id,
                "Item updated with new name: " + item.getName()
        ));

        return updated;
    }

    // ✅ Delete Single Item
    @DeleteMapping("/delete/{id}")
    public String deleteItem(@PathVariable Long id) {
        boolean deleted = itemService.deleteItem(id);

        if (deleted) {
            logService.log(
                    "DELETE",
                    "Item",
                    id,
                    "Item deleted"
            );
            return "Item deleted Successfully";
        } else {
            return "Item Not Found!";
        }
    }

    // ✅ Delete Multiple Items
    @DeleteMapping("/delete-multiple")
    public String deleteMultiple(@RequestBody List<Long> ids) {
        itemService.deleteMultipleItem(ids);

        // 🔥 Logging each deletion separately (Recommended)
        logService.log(
                "DELETE",
                "item",
                null,
                "Item deleted with IDs: " + ids
        );

        return "Items Deleted Successfully";
    }
}
