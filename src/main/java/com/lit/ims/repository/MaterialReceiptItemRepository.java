package com.lit.ims.repository;

import com.lit.ims.entity.MaterialReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialReceiptItemRepository extends JpaRepository<MaterialReceiptItem,Long> {
    List<MaterialReceiptItem> findByQcStatusAndReceipt_CompanyIdAndReceipt_BranchIdAndAdjustmentLockedFalse(
            String qcStatus, Long companyId, Long branchId);

    List<MaterialReceiptItem> findByQcStatusInAndReceipt_CompanyIdAndReceipt_BranchId(
            List<String> qcStatuses, Long companyId, Long branchId);
    Optional<MaterialReceiptItem> findByIdAndReceipt_CompanyIdAndReceipt_BranchId(Long id, Long companyId, Long branchId);
    Optional<MaterialReceiptItem> findByBatchNoAndReceipt_CompanyIdAndReceipt_BranchId(String batchNo, Long companyId, Long branchId);
    List<MaterialReceiptItem> findAllByItemCodeAndReceipt_VendorCodeAndIsIssuedFalseAndQcStatusIgnoreCaseAndReceipt_CompanyIdAndReceipt_BranchIdOrderByCreatedAtAsc(
            String itemCode,
            String vendorCode,
            String qcStatus,
            Long companyId,
            Long branchId
    );

    List<MaterialReceiptItem> findAllByReservedAtBeforeAndIsIssuedFalse(LocalDateTime cutoff);

    long countByQcStatusIgnoreCaseAndReceipt_CompanyIdAndReceipt_BranchId(String status, Long companyId, Long branchId);
    boolean existsByBatchNoAndReceipt_CompanyIdAndReceipt_BranchId(String batchNo,
                                                                   Long companyId,
                                                                   Long branchId);
    Optional<MaterialReceiptItem> findByItemCodeAndReceipt_CompanyIdAndReceipt_BranchId(String itemCode, Long companyId, Long branchId);
    List<MaterialReceiptItem> findByReceiptId(Long receiptId);
    @Query("SELECT mri FROM MaterialReceiptItem mri " +
            "WHERE DATE(mri.receipt.createdAt) = :date " +
            "AND mri.receipt.companyId = :companyId " +
            "AND mri.receipt.branchId = :branchId")
    List<MaterialReceiptItem> findByReceiptDateAndCompanyIdAndBranchId(
            @Param("date") LocalDate date,
            @Param("companyId") Long companyId,
            @Param("branchId") Long branchId);







}
