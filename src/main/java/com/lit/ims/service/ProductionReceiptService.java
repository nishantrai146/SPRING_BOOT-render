package com.lit.ims.service;


import com.lit.ims.dto.ConfirmReceiptDTO;
import com.lit.ims.entity.*;
import com.lit.ims.repository.IssuedBatchItemsRepository;
import com.lit.ims.repository.ProductionReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductionReceiptService {
    private final ProductionReceiptRepository receiptRepo;
    private final IssuedBatchItemsRepository issuedRepo;
//    private final TransactionLog logService;

    @Transactional
    public void confirmReceipt(ConfirmReceiptDTO dto,
                               Long companyId,
                               Long branchId,
                               String username) {

        /* ---------- Build header -------------------------------- */
        ProductionReceipt header = ProductionReceipt.builder()
                .transactionNumber(dto.getTransactionNumber())
                .issueNumber(dto.getIssueNumber())
                .requisitionNumber(dto.getRequisitionNumber())
                .issueDate(dto.getIssueDate())
                .receiptDate(dto.getReceiptDate())
                .companyId(companyId)
                .branchId(branchId)
                .createdBy(username)
                .build();

        /* ---------- Build rows ---------------------------------- */
        List<ProductionReceiptItem> rows = dto.getItems().stream()
                .map(line -> {

                    if (line.getReceivedQty() == null) {
                        throw new IllegalArgumentException(
                                "Received quantity is missing for item "
                                        + line.getItemCode() + " / batch " + line.getBatchNumber());
                    }

                    /* Fast lookup by primary key id */
                    IssuedBatchItems issued = issuedRepo
                            .findByIdAndIssue_CompanyIdAndIssue_BranchId(
                                    line.getId(), companyId, branchId)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Issue row not found: " + line.getId()));

                    /* Validate qty */
                    double variance = issued.getIssuedQty() - line.getReceivedQty();
                    if (variance < 0) {
                        throw new IllegalArgumentException(
                                "Received qty exceeds issued qty for row " + line.getId());
                    }

                    /* Update issue status */
                    if (variance == 0) {
                        issued.getIssue().setStatus(IssueStatus.COMPLETED);
                    } else {
                        issued.getIssue().setStatus(IssueStatus.PARTIAL);
                    }

                    /* Persist new variance on issued row */
                    issued.setVariance(variance);

                    /* Map to receipt item */
                    return ProductionReceiptItem.builder()
                            .itemCode(issued.getItemCode())
                            .itemName(issued.getItemName())
                            .batchNumber(issued.getBatchNo())
                            .issuedQuantity(issued.getIssuedQty())
                            .receivedQuantity(line.getReceivedQty())
                            .variance(variance)
                            .note(line.getNotes())
                            .receipt(header)
                            .build();
                })
                .toList();

        /* ---------- Save header (+ rows via cascade) ------------- */
        header.setItems(rows);
        receiptRepo.save(header);
    }


}
