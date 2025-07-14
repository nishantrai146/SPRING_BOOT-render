package com.lit.ims.repository;

import com.lit.ims.entity.MaterialReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaterialReceiptRepository extends JpaRepository<MaterialReceipt,Long> {
    @Query("""
    SELECT MAX(m.batchNo)
    FROM MaterialReceiptItem m
    WHERE m.batchNo LIKE CONCAT(:prefix, '%')
    AND m.receipt.companyId = :companyId
    AND m.receipt.branchId = :branchId
""")
    String findMaxBatchNoWithPrefix(@Param("prefix") String prefix,
                                    @Param("companyId") Long companyId,
                                    @Param("branchId") Long branchId);


}
