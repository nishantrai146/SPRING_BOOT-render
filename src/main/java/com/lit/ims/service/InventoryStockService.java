package com.lit.ims.service;

import com.lit.ims.entity.InventoryStock;
import com.lit.ims.entity.Warehouse;
import com.lit.ims.repository.InventoryStockRepository;
import com.lit.ims.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryStockService {

    private final InventoryStockRepository inventoryStockRepository;
    private final WarehouseRepository warehouseRepository;

    public void addStock(String itemCode, String itemName, Long warehouseId, Integer quantity, Long companyId, Long branchId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Warehouse not found with ID: " + warehouseId));

        Optional<InventoryStock> optional = inventoryStockRepository.findByItemCodeAndWarehouseIdAndCompanyIdAndBranchId(
                itemCode, warehouseId, companyId, branchId
        );

        InventoryStock stock = optional.orElse(
                InventoryStock.builder()
                        .itemCode(itemCode)
                        .itemName(itemName)
                        .warehouse(warehouse)
                        .quantity(0)
                        .companyId(companyId)
                        .branchId(branchId)
                        .build()
        );

        stock.setQuantity(stock.getQuantity() + quantity);
        inventoryStockRepository.save(stock);
    }

    public void removeStock(String itemCode, Long warehouseId, Integer quantity, Long companyId, Long branchId) {
        InventoryStock stock = inventoryStockRepository
                .findByItemCodeAndWarehouseIdAndCompanyIdAndBranchId(itemCode, warehouseId, companyId, branchId)
                .orElseThrow(() -> new RuntimeException("Stock not found to remove"));

        stock.setQuantity(stock.getQuantity() - quantity);
        inventoryStockRepository.save(stock);
    }
}
