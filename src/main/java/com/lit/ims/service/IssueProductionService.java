package com.lit.ims.service;

import com.lit.ims.dto.*;
import com.lit.ims.entity.*;
import com.lit.ims.exception.ResourceNotFoundException;
import com.lit.ims.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class IssueProductionService {

    private final IssueProductionRepository issueProductionRepository;
    private final MaterialRequisitionRepository materialRequisitionRepository;
    private final StockTransactionLogService stockTransactionLogService;
    private final WarehouseTransferLogService warehouseTransferLogService;
    private final WarehouseRepository warehouseRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final MaterialReceiptItemRepository materialReceiptItemRepository;

    public IssueProduction saveIssueProduction(IssueProductionDTO dto, Long companyId, Long branchId, String username) {

        // 1. Fetch the requisition
        MaterialRequisitions requisition = materialRequisitionRepository
                .findByTransactionNumberAndCompanyIdAndBranchId(
                        dto.getRequisitionNumber(), companyId, branchId)
                .orElseThrow(() -> new EntityNotFoundException("Requisition " + dto.getRequisitionNumber() + " not found"));
        Warehouse destinationWarehouse = requisition.getWarehouse();
        if (destinationWarehouse == null) {
            throw new ResourceNotFoundException("Destination warehouse not defined in requisition");
        }
        // 2. Build and save the IssueProduction entity
        IssueProduction issue = IssueProduction.builder()
                .issueNumber(dto.getIssueNumber())
                .requisitionNumber(dto.getRequisitionNumber())
                .companyId(companyId)
                .branchId(branchId)
                .createdBy(username)
                .issueDate(LocalDateTime.now())
                .status(IssueStatus.PENDING)
                .type(requisition.getType())
                .warehouse(destinationWarehouse)
                .build();

        List<IssuedBatchItems> batchItems = dto.getBatchItems().stream()
                .map(batch -> toEntity(batch, issue))
                .toList();

        issue.setBatchItems(batchItems);
        IssueProduction savedIssue = issueProductionRepository.save(issue);

        // 3. Update requisition status
        Map<String, Double> issuedQtyMap = batchItems.stream()
                .collect(Collectors.groupingBy(
                        IssuedBatchItems::getItemCode,
                        Collectors.summingDouble(batch -> batch.getIssuedQty() != null ? batch.getIssuedQty() : 0.0)
                ));

        boolean allFullyIssued = true;
        boolean anyIssued = false;

        for (MaterialRequisitionItem item : requisition.getItems()) {
            String itemCode = item.getCode();
            int requestedQty = item.getQuantity() != null ? item.getQuantity() : 0;
            double totalIssuedQty = issuedQtyMap.getOrDefault(itemCode, 0.0);

            // If already issued, don’t touch it. If not, check if it’s now fully issued.
            if (!Boolean.TRUE.equals(item.getIsIssued()) && totalIssuedQty >= requestedQty) {
                item.setIsIssued(true); // ✅ update only if going from false to true
            }

            if (item.getIsIssued() != null && item.getIsIssued()) {
                // This item is already fully issued
            } else if (totalIssuedQty > 0) {
                anyIssued = true;
                allFullyIssued = false;
            } else {
                allFullyIssued = false;
            }
        }

        if (allFullyIssued) {
            requisition.setStatus(RequisitionStatus.APPROVED);
        } else if (anyIssued) {
            requisition.setStatus(RequisitionStatus.PARTIALLY_ISSUED);
        } else {
            requisition.setStatus(RequisitionStatus.PENDING);
        }

        materialRequisitionRepository.save(requisition);
//        requisition.setStatus(RequisitionStatus.APPROVED);
//        materialRequisitionRepository.save(requisition);

        // 4. Get Store warehouse (from where we issue)
        Warehouse storeWarehouse = warehouseRepository.findByTypeAndCompanyIdAndBranchId(WarehouseType.STR, companyId, branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Store warehouse not found"));



        // 6. Process each batch item
        for (IssuedBatchItems batch : batchItems) {

            String itemCode = batch.getItemCode();
            String itemName = batch.getItemName();
            int qtyIssued = batch.getIssuedQty() != null ? batch.getIssuedQty().intValue() : 0;

            // 6.1 Reduce inventory from store
            InventoryStock storeStock = inventoryStockRepository
                    .findByItemCodeAndWarehouseIdAndCompanyIdAndBranchId(itemCode, storeWarehouse.getId(), companyId, branchId)
                    .orElseThrow(() -> new ResourceNotFoundException("No inventory found for item " + itemCode));

            if (storeStock.getQuantity() < qtyIssued) {
                throw new IllegalStateException("Insufficient stock for item " + itemCode);
            }

            storeStock.setQuantity(storeStock.getQuantity() - qtyIssued);
            inventoryStockRepository.save(storeStock);

            // 6.2 Add inventory to destination warehouse
            InventoryStock destinationStock = inventoryStockRepository
                    .findByItemCodeAndWarehouseIdAndCompanyIdAndBranchId(itemCode, destinationWarehouse.getId(), companyId, branchId)
                    .orElse(InventoryStock.builder()
                            .itemCode(itemCode)
                            .itemName(itemName)
                            .warehouse(destinationWarehouse)
                            .companyId(companyId)
                            .branchId(branchId)
                            .quantity(0)
                            .build());

            destinationStock.setQuantity(destinationStock.getQuantity() + qtyIssued);
            inventoryStockRepository.save(destinationStock);

            // 6.3 Log inventory reduction from store
            stockTransactionLogService.logTransaction(
                    itemCode,
                    itemName,
                    -qtyIssued,
                    "ISSUE_TO_PRODUCTION",
                    "ISSUE_PRODUCTION",
                    savedIssue.getId(),
                    companyId,
                    branchId,
                    storeWarehouse,
                    username
            );

            // 6.4 Log inventory addition to destination
            stockTransactionLogService.logTransaction(
                    itemCode,
                    itemName,
                    qtyIssued,
                    "ISSUE_TO_PRODUCTION",
                    "ISSUE_PRODUCTION",
                    savedIssue.getId(),
                    companyId,
                    branchId,
                    destinationWarehouse,
                    username
            );

            // 6.5 Log transfer between warehouses
            warehouseTransferLogService.logTransfer(
                    itemCode,
                    itemName,
                    qtyIssued,
                    storeWarehouse.getId(),
                    storeWarehouse.getName(),
                    destinationWarehouse.getId(),
                    destinationWarehouse.getName(),
                    "ISSUE_TO_PRODUCTION",
                    "ISSUE_PRODUCTION",
                    savedIssue.getId(),
                    companyId,
                    branchId,
                    username
            );

            materialReceiptItemRepository
                    .findByBatchNoAndReceipt_CompanyIdAndReceipt_BranchId(batch.getBatchNo(),companyId,branchId)
                    .ifPresent(receiptItem -> {
                        receiptItem.setWarehouse(destinationWarehouse);
                        materialReceiptItemRepository.save(receiptItem);
                    });
        }

        return savedIssue;
    }




    private IssuedBatchItems toEntity(IssuedBatchItemDTO dto, IssueProduction issue) {
        return IssuedBatchItems.builder()
                .itemCode(dto.getItemCode())
                .itemName(dto.getItemName())
                .batchNo(dto.getBatchNo())
                .quantity(dto.getQuantity())
                .issuedQty(dto.getIssuedQty())
                .variance(dto.getVariance())
                .issue(issue)
                .build();
    }


    public List<String> getAllIssueNumbers(Long companyId, Long branchId) {
        return issueProductionRepository
                .findAllByStatusAndCompanyIdAndBranchId(IssueStatus.PENDING,companyId,branchId)
                .stream()
                .map(IssueProduction::getIssueNumber)
                .distinct()
                .toList();
    }

    public List<String> getIssueNumberCompleted(Long companyId,Long branchId){
        return issueProductionRepository
                .findAllByStatusAndCompanyIdAndBranchId(IssueStatus.COMPLETED,companyId,branchId)
                .stream()
                .map(IssueProduction::getIssueNumber)
                .distinct()
                .toList();
    }

    public IssuedItemSummaryResponseDTO getIssuedBatchesWithMeta(
            String issueNumber, Long companyId, Long branchId) {

        IssueProduction issue = issueProductionRepository
                .findByIssueNumberAndCompanyIdAndBranchId(issueNumber, companyId, branchId)
                .orElse(null);

        if (issue == null) return null;

        String reqNumber = issue.getRequisitionNumber();
        LocalDateTime issueDate = issue.getIssueDate();
        LocalDateTime requisitionCreatedAt = materialRequisitionRepository
                .findByTransactionNumberAndCompanyIdAndBranchId(reqNumber, companyId, branchId)
                .map(MaterialRequisitions::getCreatedAt)
                .orElse(null);

        /* 🔄 Build a flat list: one DTO per batch row */
        List<IssuedItemSummaryDTO> items = issue.getBatchItems().stream()
                .map(b -> IssuedItemSummaryDTO.builder()
                        .id(b.getId())
                        .itemCode(b.getItemCode())
                        .itemName(b.getItemName())
                        .totalIssued(b.getQuantity())
                        .totalVariance(b.getVariance())
                        .batchNumber(b.getBatchNo())
                        .build())
                .toList();

        return IssuedItemSummaryResponseDTO.builder()
                .issueNumber(issueNumber)
                .requisitionNumber(reqNumber)
                .requisitionCreatedAt(requisitionCreatedAt)
                .issueDate(issueDate)
                .type(issue.getType())
                .items(items)                   // 🆕 flat list
                .build();
    }




}
