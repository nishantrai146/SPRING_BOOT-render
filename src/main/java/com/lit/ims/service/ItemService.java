package com.lit.ims.service;

import com.lit.ims.dto.ItemDTO;
import com.lit.ims.entity.Item;
import com.lit.ims.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final TransactionLogService transactionLogService;

    // ✅ Create Item
    public Item saveItem(ItemDTO request, Long companyId, Long branchId) {
        boolean exists = itemRepository.existsByCodeAndCompanyIdAndBranchId(
                request.getCode(), companyId, branchId
        );

        if (exists) {
            throw new RuntimeException("Item code already exists in this branch.");
        }

        Item item = Item.builder()
                .name(request.getName())
                .code(request.getCode())
                .uom(request.getUom())
                .groupName(request.getGroupName())
                .status(request.getStatus())
                .price(request.getPrice())
                .stQty(request.getStQty())
                .life(request.getLife())
                .companyId(companyId)
                .branchId(branchId)
                .isInventoryItem(request.isInventoryItem())
                .isIqc(request.isIqc())
                .build();

        Item savedItem = itemRepository.save(item);

        transactionLogService.log("CREATE", "Item", savedItem.getId(),
                "Created item with code: " + savedItem.getCode());

        return savedItem;
    }

    // ✅ Get All Items
    public List<Item> getAllItems(Long companyId, Long branchId) {
        return itemRepository.findByCompanyIdAndBranchId(companyId, branchId);
    }

    // ✅ Get Item by ID
    public Optional<Item> getItemById(Long id, Long companyId, Long branchId) {
        return itemRepository.findByIdAndCompanyIdAndBranchId(id, companyId, branchId);
    }

    // ✅ Update Item
    public Optional<Item> updateItem(Long id, ItemDTO dto, Long companyId, Long branchId) {
        return itemRepository.findByIdAndCompanyIdAndBranchId(id, companyId, branchId)
                .map(item -> {
                    if (itemRepository.existsByCodeAndCompanyIdAndBranchIdAndIdNot(
                            dto.getCode(), companyId, branchId, id)) {
                        throw new RuntimeException("Item code already exists in this branch.");
                    }

                    item.setName(dto.getName());
                    item.setCode(dto.getCode());
                    item.setUom(dto.getUom());
                    item.setGroupName(dto.getGroupName());
                    item.setStatus(dto.getStatus());
                    item.setPrice(dto.getPrice());
                    item.setStQty(dto.getStQty());
                    item.setLife(dto.getLife());
                    item.setInventoryItem(dto.isInventoryItem());
                    item.setIqc(dto.isIqc());

                    Item updated = itemRepository.save(item);

                    transactionLogService.log("UPDATE", "Item", updated.getId(),
                            "Updated item with code: " + updated.getCode());

                    return updated;
                });
    }

    // ✅ Delete Single Item
    public boolean deleteItem(Long id, Long companyId, Long branchId) {
        Optional<Item> item = itemRepository.findByIdAndCompanyIdAndBranchId(id, companyId, branchId);
        if (item.isPresent()) {
            itemRepository.deleteById(id);

            transactionLogService.log("DELETE", "Item", id,
                    "Deleted item with ID: " + id);

            return true;
        } else {
            throw new RuntimeException("Item not found");
        }
    }

    // ✅ Delete Multiple Items
    public void deleteMultipleItems(List<Long> ids, Long companyId, Long branchId) {
        List<Item> items = itemRepository.findAllById(ids).stream()
                .filter(item -> item.getCompanyId().equals(companyId) && item.getBranchId().equals(branchId))
                .toList();

        if (items.isEmpty()) {
            throw new RuntimeException("No valid items found to delete.");
        }

        itemRepository.deleteAll(items);

        for (Item item : items) {
            transactionLogService.log("DELETE", "Item", item.getId(),
                    "Deleted item with ID: " + item.getId());
        }
    }
}
