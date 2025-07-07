package com.lit.ims.repository;

import com.lit.ims.entity.MaterialReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialReceiptItemRepository extends JpaRepository<MaterialReceiptItem,Long> {
    List<MaterialReceiptItem> findByQcStatusAndReceipt_CompanyIdAndReceipt_BranchId(String qcStatus, Long companyId, Long branchId);
}
