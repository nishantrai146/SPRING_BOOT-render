package com.lit.ims.repository;

import com.lit.ims.entity.MaterialRequisitions;
import com.lit.ims.entity.RequisitionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaterialRequisitionRepository extends JpaRepository<MaterialRequisitions, Long> {
    List<MaterialRequisitions> findTop10ByCompanyIdAndBranchIdOrderByCreatedAtDesc(Long companyId, Long branchId);
    List<MaterialRequisitions> findByCompanyIdAndBranchIdAndStatus(Long companyId, Long branchId, RequisitionStatus status);
    Optional<MaterialRequisitions> findByTransactionNumberAndCompanyIdAndBranchId(String transactionNumber, Long companyId, Long branchId);

}
