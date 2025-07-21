package com.lit.ims.repository;

import com.lit.ims.entity.StockTransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StockTransactionLogRepository extends JpaRepository<StockTransactionLog, Long> {

    @Query("SELECT s FROM StockTransactionLog s " +
            "WHERE s.companyId = :companyId AND s.branchId = :branchId " +
            "AND (:itemCode IS NULL OR s.itemCode = :itemCode) " +
            "AND (:warehouseId IS NULL OR s.warehouse.id = :warehouseId) " +
            "AND (:transactionType IS NULL OR s.transactionType = :transactionType) " +
            "AND (:fromDate IS NULL OR s.transactionDate >= :fromDate) " +
            "AND (:toDate IS NULL OR s.transactionDate <= :toDate)")
    List<StockTransactionLog> findWithFilters(
            @Param("companyId") Long companyId,
            @Param("branchId") Long branchId,
            @Param("itemCode") String itemCode,
            @Param("warehouseId") Long warehouseId,
            @Param("transactionType") String transactionType,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );
}
