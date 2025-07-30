package com.lit.ims.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lit.ims.dto.WipReturnDTO;
import com.lit.ims.entity.*;
import com.lit.ims.enums.ApprovalStage;
import com.lit.ims.enums.ApprovalStatus;
import com.lit.ims.enums.ReferenceType;
import com.lit.ims.repository.ApprovalsRepository;
import com.lit.ims.repository.BatchSequenceTrackerRepository;
import com.lit.ims.repository.UserRepository;
import com.lit.ims.repository.WipReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WipReturnService {

    private final WipReturnRepository wipReturnRepository;
    private final BatchSequenceTrackerRepository trackerRepository;
    private final ApprovalsRepository approvalsRepository;
    private final UserRepository userRepository;


    @Transactional
    public WipReturn saveWipReturn(WipReturnDTO dto, Long companyId, Long branchId, String username) {
        WipReturn wipReturn = WipReturn.builder()
                .transactionNumber(dto.getTransactionNumber())
                .returnType(dto.getReturnType())
                .returnDate(dto.getReturnDate())
                .workOrderId(dto.getWorkOrderId())
                .receiptNumber(dto.getReceiptNumber())
                .warehouseId(dto.getWarehouseId())
                .companyId(companyId)
                .branchId(branchId)
                .createdBy(username)
                .createdAt(LocalDateTime.now())
                .approvalStatus(ApprovalStatus.PENDING)
                .build();

        List<WipReturnItem> items = dto.getReturnItems().stream()
                .map(i -> {
                    String originalBatchNo = i.getBatchNo();
                    String vendorCode = originalBatchNo.substring(1, 7);
                    String itemCode = originalBatchNo.substring(7, 13);
                    String returnQty = String.format("%03d", i.getReturnQty());
                    String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
                    String batchPrefix = "W" + vendorCode + itemCode + returnQty + datePart;

                    BatchSequenceTracker tracker = trackerRepository
                            .findByBatchPrefixAndCompanyIdAndBranchId(batchPrefix, companyId, branchId)
                            .orElseGet(() -> BatchSequenceTracker.builder()
                                    .batchPrefix(batchPrefix)
                                    .lastSequence(0)
                                    .companyId(companyId)
                                    .branchId(branchId)
                                    .build());

                    int nextSeq = tracker.getLastSequence() + 1;
                    tracker.setLastSequence(nextSeq);
                    trackerRepository.save(tracker);

                    String sequenceStr = String.format("%05d", nextSeq);
                    String newBatchNo = batchPrefix + sequenceStr;

                    return WipReturnItem.builder()
                            .itemCode(i.getItemCode())
                            .itemName(i.getItemName())
                            .originalBatchNo(originalBatchNo)
                            .newBatchNo(newBatchNo)
                            .originalQty(i.getOriginalQty())
                            .returnQty(i.getReturnQty())
                            .returnReason(i.getReturnReason())
                            .wipReturn(wipReturn)
                            .build();
                })
                .collect(Collectors.toList());

        wipReturn.setReturnItems(items);
        WipReturn savedReturn = wipReturnRepository.save(wipReturn);

        StringBuilder itemDetails = new StringBuilder();
        for (WipReturnItem item : savedReturn.getReturnItems()) {
            itemDetails.append(String.format(
                    "- %s (Code: %s) | Returned Qty: %d | New Batch No: %s\n",
                    item.getItemName(),
                    item.getItemCode(),
                    item.getReturnQty(),
                    item.getNewBatchNo()
            ));
        }

        String metaData = String.format(
                "WIP Return Request\n" +
                        "Transaction No: %s\n" +
                        "Return Type: %s\n" +
                        "Return Date: %s\n" +
                        "Work Order ID: %d\n" +
                        "Receipt No: %s\n" +
                        "Warehouse ID: %d\n" +
                        "Items Returned:\n%s",
                savedReturn.getTransactionNumber(),
                savedReturn.getReturnType(),
                savedReturn.getReturnDate(),
                savedReturn.getWorkOrderId(),
                savedReturn.getReceiptNumber(),
                savedReturn.getWarehouseId(),
                itemDetails.toString()
        );

        // 🔁 CREATE FIRST APPROVAL REQUEST for Production Head
        Approvals approval = Approvals.builder()
                .referenceType(ReferenceType.WIP_RETURN)
                .referenceId(savedReturn.getId())
                .requestedTo(findApprover("PRODUCTION", Role.ADMIN, companyId, branchId))
                .status(ApprovalStatus.PENDING)
                .requestedBy(username)
                .requestedDate(LocalDateTime.now())
                .companyId(companyId)
                .branchId(branchId)
                .stage(ApprovalStage.PRODUCTION_HEAD)
                .metaData(metaData)
                .build();

        approvalsRepository.save(approval);

        return savedReturn;
    }

    private String findApprover(String department, Role role, Long companyId, Long branchId) {
        return userRepository.findFirstApproverIgnoreCase(role, department, companyId, branchId)
                .map(User::getUsername)
                .orElseThrow(() -> new RuntimeException("Approver not found for department " + department));
    }

    public List<Map<String, Object>> getRecentWipReturnSummary(Long companyId, Long branchId) {
        List<WipReturn> recentList = wipReturnRepository
                .findTop10ByCompanyIdAndBranchIdOrderByCreatedAtDesc(companyId, branchId);

        return recentList.stream().map(recent -> {
            Map<String, Object> response = new HashMap<>();
            response.put("date", recent.getReturnDate());
            response.put("transactionNumber", recent.getTransactionNumber());
            response.put("receiptNumber", recent.getReceiptNumber());
            response.put("workOrderId", recent.getWorkOrderId());
            response.put("returnType", recent.getReturnType());

            List<String> itemNames = recent.getReturnItems()
                    .stream()
                    .map(WipReturnItem::getItemName)
                    .toList();

            int totalValue = recent.getReturnItems()
                    .stream()
                    .mapToInt(item -> item.getReturnQty() != null ? item.getReturnQty() : 0)
                    .sum();

            response.put("items", itemNames);
            response.put("totalValue", totalValue);
            response.put("status", "Submitted");

            return response;
        }).toList();
    }
}
