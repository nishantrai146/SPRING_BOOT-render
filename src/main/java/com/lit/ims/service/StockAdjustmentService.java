package com.lit.ims.service;

import com.lit.ims.dto.*;
import com.lit.ims.entity.*;
import com.lit.ims.exception.NotFoundException;
import com.lit.ims.repository.*;
import com.lit.ims.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockAdjustmentService {

    private final MaterialReceiptItemRepository itemRepo;
    private final StockAdjustmentRequestRepository reqRepo;
    private final StockAdjustmentRepository adjRepo;
    private final TransactionLogService logService;

    /* ----------------------------------------------------------------
       OPERATOR:  create a quantity‑change request
       ---------------------------------------------------------------- */
    @Transactional
    public ApiResponse<AdjustmentRequestResponseDTO> createRequest(
            CreateAdjustmentRequestDTO dto,
            Long companyId,
            Long branchId,
            String username) {

        MaterialReceiptItem item = itemRepo
                .findByBatchNoAndReceipt_CompanyIdAndReceipt_BranchId(
                        dto.getBatchNo(), companyId, branchId)
                .orElseThrow(() -> new NotFoundException("Batch not found"));

        if (item.isIssued())                 // cannot change an already‑issued batch
            return ApiResponse.fail("Batch already issued – quantity cannot be changed");

        if (item.isAdjustmentLocked())
            return ApiResponse.fail("A quantity‑change request is already pending");

        /* Lock the batch immediately so nobody can issue it until admin decides */
        item.setAdjustmentLocked(true);
        itemRepo.save(item);

        StockAdjustmentRequest req = reqRepo.save(
                StockAdjustmentRequest.builder()
                        .batchNo(dto.getBatchNo())
                        .oldQty(item.getQuantity())
                        .requestedQty(dto.getRequestedQty())
                        .diff(dto.getRequestedQty() - item.getQuantity())
                        .reason(dto.getReason())
                        .status(AdjustmentStatus.PENDING)
                        .requestedBy(username)
                        .requestedAt(LocalDateTime.now())
                        .companyId(companyId)
                        .branchId(branchId)
                        .build());

//        logService.logAction("StockAdjustmentRequest", req.getId(),
//                ActionType.CREATE, "Qty change requested "
//                        + req.getOldQty() + " → " + req.getRequestedQty());

        return ApiResponse.ok(toDTO(req));
    }

    /* ----------------------------------------------------------------
       ADMIN:  approve the request
       ---------------------------------------------------------------- */
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    public ApiResponse<Void> approve(Long requestId, String approver) {

        StockAdjustmentRequest req = reqRepo.findByIdForUpdate(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        if (req.getStatus() != AdjustmentStatus.PENDING)
            return ApiResponse.fail("Request already processed");

        MaterialReceiptItem item = itemRepo
                .findByBatchNoAndReceipt_CompanyIdAndReceipt_BranchId(
                        req.getBatchNo(), req.getCompanyId(), req.getBranchId())
                .orElseThrow(() -> new NotFoundException("Batch not found"));

        if (item.isIssued())
            return ApiResponse.fail("Cannot approve – batch has been issued meanwhile");

        /* Apply the new quantity and unlock */
        int oldQty = item.getQuantity();
        item.setQuantity(req.getRequestedQty());
        item.setAdjustmentLocked(false);
        itemRepo.save(item);

        adjRepo.save(StockAdjustment.builder()
                .batchNo(req.getBatchNo())
                .oldQty (oldQty)
                .newQty(req.getRequestedQty())
                .diff(req.getDiff())
                .reason(req.getReason())
                .adjustedBy(approver)
                .timestamp(LocalDateTime.now())
                .companyId(req.getCompanyId())
                .branchId(req.getBranchId())
                .build());

        req.setStatus(AdjustmentStatus.APPROVED);
        req.setApprovedBy(approver);
        req.setApprovedAt(LocalDateTime.now());
        reqRepo.save(req);

//        logService.logAction("MaterialReceiptItem", item.getId(),
//                ActionType.UPDATE, "Qty adjusted via approval "
//                        + oldQty + " → " + item.getQuantity());

        return ApiResponse.ok();
    }

    /* ----------------------------------------------------------------
       ADMIN:  reject the request
       ---------------------------------------------------------------- */
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    public ApiResponse<Void> reject(Long requestId, String approver, String reason) {

        StockAdjustmentRequest req = reqRepo.findByIdForUpdate(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        if (req.getStatus() != AdjustmentStatus.PENDING)
            return ApiResponse.fail("Request already processed");

        MaterialReceiptItem item = itemRepo
                .findByBatchNoAndReceipt_CompanyIdAndReceipt_BranchId(
                        req.getBatchNo(), req.getCompanyId(), req.getBranchId())
                .orElseThrow(() -> new NotFoundException("Batch not found"));

        /* Simply unlock the batch – no qty change */
        item.setAdjustmentLocked(false);
        itemRepo.save(item);

        req.setStatus(AdjustmentStatus.REJECTED);
        req.setApprovedBy(approver);
        req.setApprovedAt(LocalDateTime.now());
        reqRepo.save(req);

//        logService.logAction("StockAdjustmentRequest", req.getId(),
//                ActionType.UPDATE, "Rejected: " + reason);

        return ApiResponse.ok();
    }

    /* ----------------------------------------------------------------
       ADMIN:  list requests by status
       ---------------------------------------------------------------- */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    public List<AdjustmentRequestResponseDTO> findByStatus(
            AdjustmentStatus status,
            Long companyId,
            Long branchId) {

        return reqRepo
                .findByStatusAndCompanyIdAndBranchId(status, companyId, branchId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /* ----------------------------------------------------------------
       Mapper
       ---------------------------------------------------------------- */
    private AdjustmentRequestResponseDTO toDTO(StockAdjustmentRequest r) {
        return AdjustmentRequestResponseDTO.builder()
                .id(r.getId())
                .batchNo(r.getBatchNo())
                .oldQty(r.getOldQty())
                .requestedQty(r.getRequestedQty())
                .diff(r.getDiff())
                .reason(r.getReason())
                .status(r.getStatus())
                .requestedBy(r.getRequestedBy())
                .requestedAt(r.getRequestedAt())
                .build();
    }
}
