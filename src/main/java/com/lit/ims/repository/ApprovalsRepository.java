package com.lit.ims.repository;

import com.lit.ims.entity.Approvals;
import com.lit.ims.enums.ReferenceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalsRepository extends JpaRepository<Approvals, Long> {

    List<Approvals> findByRequestedToAndCompanyIdAndBranchId(String username, Long companyId, Long branchId);

    List<Approvals> findByReferenceTypeAndReferenceIdAndCompanyIdAndBranchId(
            ReferenceType type, Long referenceId, Long companyId, Long branchId
    );
    List<Approvals> findByCompanyIdAndBranchId(Long companyId, Long branchId);
}
