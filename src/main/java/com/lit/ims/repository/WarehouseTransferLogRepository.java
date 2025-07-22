package com.lit.ims.repository;

import com.lit.ims.entity.WarehouseTransferLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface WarehouseTransferLogRepository extends JpaRepository<WarehouseTransferLog, Long> {
    @Query("SELECT l FROM WarehouseTransferLog l " +
            "WHERE l.companyId = :companyId AND l.branchId = :branchId " +
            "AND (:itemCode IS NULL OR l.itemCode = :itemCode) " +
            "AND (:sourceWarehouseId IS NULL OR l.sourceWarehouseId = :sourceWarehouseId) " +
            "ORDER BY l.transferredAt DESC")
    List<WarehouseTransferLog> findByItemAndSourceWarehouse(
            @Param("companyId") Long companyId,
            @Param("branchId") Long branchId,
            @Param("itemCode") String itemCode,
            @Param("sourceWarehouseId") Long sourceWarehouseId
    );


}
