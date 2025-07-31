package com.lit.ims.repository;

import com.lit.ims.entity.ProductionReceipt;
import com.lit.ims.enums.WipReturnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionReceiptRepository extends JpaRepository<ProductionReceipt,Long> {
    /* ProductionReceiptRepository.java */
    List<ProductionReceipt> findByCompanyIdAndBranchIdOrderByReceiptDateDesc(
            Long companyId, Long branchId);
    Optional<ProductionReceipt> findByTransactionNumberAndCompanyIdAndBranchId(
            String transactionNumber,
            Long companyId,
            Long branchId
    );

    List<ProductionReceipt> findByCompanyIdAndBranchIdAndWipReturnStatus(
            Long companyId,
            Long branchId,
            WipReturnStatus wipReturnStatus
    );



}
