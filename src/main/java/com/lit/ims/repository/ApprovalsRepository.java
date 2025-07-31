package com.lit.ims.repository;

import com.lit.ims.entity.Approvals;
import com.lit.ims.enums.ApprovalStatus;
import com.lit.ims.enums.ReferenceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalsRepository extends JpaRepository<Approvals, Long> {

    List<Approvals> findByCompanyIdAndBranchId(Long companyId, Long branchId);
    List<Approvals> findByCompanyIdAndBranchIdAndStatus(Long companyId, Long branchId, ApprovalStatus status);

    List<Approvals> findByRequestedToAndCompanyIdAndBranchIdAndStatus(String requestedTo, Long companyId, Long branchId, ApprovalStatus status);


}
