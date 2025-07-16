package com.lit.ims.repository;

import com.lit.ims.entity.ProductionReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionReceiptRepository extends JpaRepository<ProductionReceipt,Long> {
    /* ProductionReceiptRepository.java */
    List<ProductionReceipt> findByCompanyIdAndBranchIdOrderByReceiptDateDesc(
            Long companyId, Long branchId);

}
