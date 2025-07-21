package com.lit.ims.repository;

import com.lit.ims.entity.InventoryStock;
import com.lit.ims.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryStockRepository extends JpaRepository<InventoryStock, Long> {
    Optional<InventoryStock> findByItemCodeAndWarehouse(String itemCode, Warehouse warehouse);
    Optional<InventoryStock> findByItemCodeAndWarehouseIdAndCompanyIdAndBranchId(
            String itemCode, Long warehouseId, Long companyId, Long branchId
    );
}
