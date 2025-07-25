package com.lit.ims.service;


import com.lit.ims.dto.*;
import com.lit.ims.entity.*;
import com.lit.ims.repository.IssuedBatchItemsRepository;
import com.lit.ims.repository.ProductionReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

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

        // -- look up one IssuedBatch row to get the Issue header (for TYPE) --
        IssueProduction issue = issuedRepo
                .findByIssue_IssueNumberAndIssue_CompanyIdAndIssue_BranchId(
                        dto.getIssueNumber(), companyId, branchId)
                .stream()
                .findFirst()
                .map(IssuedBatchItems::getIssue)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Issue not found: " + dto.getIssueNumber()));

        // -- build receipt header --
        ProductionReceipt header = ProductionReceipt.builder()
                .transactionNumber(dto.getTransactionNumber())
                .issueNumber(dto.getIssueNumber())
                .requisitionNumber(dto.getRequisitionNumber())
                .issueDate(dto.getIssueDate())
                .receiptDate(dto.getReceiptDate())
                .companyId(companyId)
                .branchId(branchId)
                .createdBy(username)
                .type(issue.getType())
                .build();

        // -- build receipt rows --
        List<ProductionReceiptItem> rows = dto.getItems().stream()
                .map(line -> {

                    if (line.getReceivedQty() == null) {
                        throw new IllegalArgumentException(
                                "Received quantity missing for item "
                                        + line.getItemCode() + " / batch " + line.getBatchNumber());
                    }

                    IssuedBatchItems issued = issuedRepo
                            .findByIdAndIssue_CompanyIdAndIssue_BranchId(
                                    line.getId(), companyId, branchId)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Issue row not found: " + line.getId()));

                    double variance = issued.getIssuedQty() - line.getReceivedQty();
                    issued.setVariance(variance);

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

        header.setItems(rows);

        // -- decide overall receipt status --
        long completedRows = rows.stream()
                .filter(r -> r.getVariance() <= 0)   // received >= issued
                .count();

        IssueStatus finalStatus;
        if (completedRows == 0) {
            finalStatus = IssueStatus.PENDING;
        } else if (completedRows == rows.size()) {
            finalStatus = IssueStatus.COMPLETED;
        } else {
            finalStatus = IssueStatus.PARTIAL;
        }

        header.setStatus(finalStatus);      // Receipt status
        issue.setStatus(finalStatus);       // Master IssueProduction status

        // -- persist everything --
        receiptRepo.save(header);
    }

    @Transactional(readOnly = true)
    public List<ProductionReceiptTableDTO> listReceiptsForTable(Long companyId,
                                                                Long branchId) {

        return receiptRepo
                .findByCompanyIdAndBranchIdOrderByReceiptDateDesc(companyId, branchId)
                .stream()
                .map(r -> {

                    // distinct itemCode + itemName pairs for the Items column
                    List<ItemCodeNameDTO> itemList = r.getItems().stream()
                            .collect(Collectors.toMap(
                                    ProductionReceiptItem::getItemCode,
                                    ProductionReceiptItem::getItemName,
                                    (n1, n2) -> n1,                 // ignore dups
                                    LinkedHashMap::new))
                            .entrySet().stream()
                            .map(e -> new ItemCodeNameDTO(e.getKey(), e.getValue()))
                            .toList();

                    return ProductionReceiptTableDTO.builder()
                            .transactionNumber(r.getTransactionNumber())
                            .receiptDate(r.getReceiptDate())
                            .type(r.getType())
                            .items(itemList)
                            .status(r.getStatus().name())
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReceiptIdNumberDTO> getAllReceiptIdAndNumbers(Long companyId, Long branchId) {
        return receiptRepo.findByCompanyIdAndBranchIdOrderByReceiptDateDesc(companyId, branchId)
                .stream()
                .map(r -> new ReceiptIdNumberDTO(r.getId(), r.getTransactionNumber()))
                .toList();
    }
    @Transactional(readOnly = true)
    public List<ProductionReceiptItemDTO> getReceiptItemsById(Long receiptId, Long companyId, Long branchId) {
        ProductionReceipt receipt = receiptRepo.findById(receiptId)
                .orElseThrow(() -> new IllegalArgumentException("Receipt not found: " + receiptId));

        // Optional: Check access for company/branch
        if (!receipt.getCompanyId().equals(companyId) || !receipt.getBranchId().equals(branchId)) {
            throw new RuntimeException("Access denied for receipt ID: " + receiptId);
        }

        return receipt.getItems().stream()
                .map(item -> new ProductionReceiptItemDTO(
                        item.getItemCode(),
                        item.getItemName(),
                        item.getBatchNumber(),
                        item.getIssuedQuantity(),
                        item.getReceivedQuantity(),
                        item.getVariance(),
                        item.getNote()
                ))
                .toList();
    }



}
