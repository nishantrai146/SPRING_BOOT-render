package com.lit.ims.repository;

import com.lit.ims.entity.IssueProduction;
import com.lit.ims.entity.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueProductionRepository extends JpaRepository<IssueProduction, Long> {
    List<IssueProduction> findByCompanyIdAndBranchId(Long companyId, Long branchId);
    Optional<IssueProduction> findByIssueNumberAndCompanyIdAndBranchId(String issueNumber, Long companyId, Long branchId);
    List<IssueProduction> findAllByStatusAndCompanyIdAndBranchId(IssueStatus status, Long companyId, Long branchId);


}
