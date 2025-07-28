package com.lit.ims.service;

import com.lit.ims.dto.WipReturnDTO;
import com.lit.ims.entity.BatchSequenceTracker;
import com.lit.ims.entity.WipReturn;
import com.lit.ims.entity.WipReturnItem;
import com.lit.ims.repository.BatchSequenceTrackerRepository;
import com.lit.ims.repository.WipReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WipReturnService {

    private final WipReturnRepository wipReturnRepository;
    private final BatchSequenceTrackerRepository trackerRepository;

    @Transactional
    public WipReturn saveWipReturn(WipReturnDTO dto, Long companyId, Long branchId, String username) {
        WipReturn wipReturn = WipReturn.builder()
                .transactionNumber(dto.getTransactionNumber())
                .returnType(dto.getReturnType())
                .returnDate(dto.getReturnDate())
                .workOrderId(dto.getWorkOrderId())
                .warehouseId(dto.getWarehouseId())
                .companyId(companyId)
                .branchId(branchId)
                .createdBy(username)
                .createdAt(LocalDateTime.now())
                .build();

        List<WipReturnItem> items = dto.getReturnItems().stream()
                .map(i -> {
                    String originalBatchNo = i.getBatchNo();
                    String vendorCode = originalBatchNo.substring(1, 7);
                    String itemCode = originalBatchNo.substring(7, 13);
                    String returnQty = String.format("%03d", i.getReturnQty()); // pad qty if needed
                    String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
                    String batchPrefix = "W" + vendorCode + itemCode + returnQty + datePart;

                    // Fetch and increment sequence
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
        return wipReturnRepository.save(wipReturn);
    }
    public List<Map<String, Object>> getRecentWipReturnSummary(Long companyId, Long branchId) {
        List<WipReturn> recentList = wipReturnRepository
                .findTop10ByCompanyIdAndBranchIdOrderByCreatedAtDesc(companyId, branchId);

        return recentList.stream().map(recent -> {
            Map<String, Object> response = new HashMap<>();
            response.put("date", recent.getReturnDate());
            response.put("transactionNumber", recent.getTransactionNumber());
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
