package com.lit.ims.repository;

import com.lit.ims.dto.IssueQuantityDTO;
import com.lit.ims.entity.IssuedBatchItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssuedBatchItemsRepository  extends JpaRepository<IssuedBatchItems,Long> {
    Optional<IssuedBatchItems> findByIdAndIssue_CompanyIdAndIssue_BranchId(
            Long id, Long companyId, Long branchId);
    List<IssuedBatchItems> findByIssue_IssueNumberAndIssue_CompanyIdAndIssue_BranchId(
            String issueNumber, Long companyId, Long branchId);
    @Query("SELECT new com.lit.ims.dto.IssueQuantityDTO(b.itemCode, SUM(b.issuedQty)) " +
            "FROM IssuedBatchItems b " +
            "WHERE b.issue.requisitionNumber = :requisitionNumber " +
            "AND b.issue.companyId = :companyId " +
            "AND b.issue.branchId = :branchId " +
            "GROUP BY b.itemCode")
    List<IssueQuantityDTO> getIssuedQuantityData(String requisitionNumber, Long companyId, Long branchId);




}
