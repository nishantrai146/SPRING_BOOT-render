package com.lit.ims.repository;

import com.lit.ims.entity.AdjustmentStatus;
import com.lit.ims.entity.StockAdjustmentRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockAdjustmentRequestRepository extends JpaRepository<StockAdjustmentRequest, Long> {

    boolean existsByBatchNoAndStatus(String batchNo, AdjustmentStatus status);

    List<StockAdjustmentRequest> findByStatusAndCompanyIdAndBranchId(
            AdjustmentStatus status, Long companyId, Long branchId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from StockAdjustmentRequest r where r.id = :id")
    Optional<StockAdjustmentRequest> findByIdForUpdate(@Param("id") Long id);
}
