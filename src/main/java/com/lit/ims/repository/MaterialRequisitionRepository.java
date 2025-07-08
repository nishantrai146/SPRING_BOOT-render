package com.lit.ims.repository;

import com.lit.ims.entity.MaterialRequisitions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialRequisitionRepository extends JpaRepository<MaterialRequisitions, Long> {
    List<MaterialRequisitions> findTop10ByCompanyIdAndBranchIdOrderByCreatedAtDesc(Long companyId, Long branchId);
}
