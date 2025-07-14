package com.lit.ims.repository;

import com.lit.ims.entity.IssuedBatchItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssuedBatchItemsRepository  extends JpaRepository<IssuedBatchItems,Long> {
    Optional<IssuedBatchItems> findByIdAndIssue_CompanyIdAndIssue_BranchId(
            Long id, Long companyId, Long branchId);
    List<IssuedBatchItems> findByIssue_IssueNumberAndIssue_CompanyIdAndIssue_BranchId(
            String issueNumber, Long companyId, Long branchId);


}
