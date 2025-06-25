package com.lit.ims.service;

import com.lit.ims.dto.ItemDTO;
import com.lit.ims.entity.Item;
import com.lit.ims.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TransactionLogService transactionLogService;

    public Item saveItem(ItemDTO request) {
        Item item = Item.builder()
                .name(request.getName())
                .code(request.getCode())
                .uom(request.getUom())
                .type(request.getType())
                .barcode((request.getBarcode() != null && !request.getBarcode().isEmpty()) ? request.getBarcode() : null)
                .groupName(request.getGroup())
                .status(request.getStatus())
                .price(request.getPrice())
                .stQty(request.getSt_qty())
                .life(request.getLife())
                .build();

        Item savedItem = itemRepository.save(item);

        // ✅ Log transaction
        transactionLogService.log("CREATE", "Item", savedItem.getId(), "Created item: " + savedItem.getName());

        return savedItem;
    }

    public List<Item> getAllItem() {
        return itemRepository.findAll();
    }

    public Optional<Item> getItemById(Long id) {
        return itemRepository.findById(id);
    }

    public Optional<Item> updateItem(Long id, ItemDTO dto) {
        return itemRepository.findById(id).map(item -> {
            item.setName(dto.getName());
            item.setCode(dto.getCode());
            item.setUom(dto.getUom());
            item.setType(dto.getType());
            item.setBarcode(dto.getBarcode() != null && !dto.getBarcode().isEmpty() ? dto.getBarcode() : null);
            item.setGroupName(dto.getGroup());
            item.setStatus(dto.getStatus());
            item.setPrice(dto.getPrice());
            item.setStQty(dto.getSt_qty());
            item.setLife(dto.getLife());

            Item updatedItem = itemRepository.save(item);

            // ✅ Log transaction
            transactionLogService.log("UPDATE", "Item", updatedItem.getId(), "Updated item: " + updatedItem.getName());

            return updatedItem;
        });
    }

    public boolean deleteItem(Long id) {
        if (itemRepository.existsById(id)) {
            itemRepository.deleteById(id);

            // ✅ Log transaction
            transactionLogService.log("DELETE", "Item", id, "Deleted item with ID: " + id);

            return true;
        }
        return false;
    }

    public void deleteMultipleItem(List<Long> ids) {
        itemRepository.deleteAllById(ids);

        // ✅ Log each deletion
        for (Long id : ids) {
            transactionLogService.log("DELETE", "Item", id, "Deleted item with ID: " + id);
        }
    }
}
