package com.lit.ims.repository;

import com.lit.ims.entity.MaterialReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialReceiptItemRepository extends JpaRepository<MaterialReceiptItem,Long> {
    List<MaterialReceiptItem> findByQcStatusAndReceipt_CompanyIdAndReceipt_BranchId(String qcStatus, Long companyId, Long branchId);
    List<MaterialReceiptItem> findByQcStatusInAndReceipt_CompanyIdAndReceipt_BranchId(
            List<String> qcStatuses, Long companyId, Long branchId);
    Optional<MaterialReceiptItem> findByIdAndReceipt_CompanyIdAndReceipt_BranchId(Long id, Long companyId, Long branchId);

}
