package com.lit.ims.service;

import com.lit.ims.dto.WarehouseTransferLogDTO;
import com.lit.ims.dto.WarehouseTransferLogFilterDTO;
import com.lit.ims.entity.StockTransactionSequenceTracker;
import com.lit.ims.entity.WarehouseTransferLog;
import com.lit.ims.repository.StockTransactionSequenceTrackerRepository;
import com.lit.ims.repository.WarehouseTransferLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseTransferLogService {

    private final WarehouseTransferLogRepository transferLogRepository;
    private final StockTransactionSequenceTrackerRepository sequenceTrackerRepository;

    private synchronized String generateTrNo() {
        LocalDate today = LocalDate.now();
        StockTransactionSequenceTracker tracker = sequenceTrackerRepository
                .findByDate(today)
                .orElse(StockTransactionSequenceTracker.builder()
                        .date(today)
                        .sequence(0)
                        .build());

        int nextSeq = tracker.getSequence() + 1;
        tracker.setSequence(nextSeq);
        sequenceTrackerRepository.save(tracker);

        String datePart = today.format(DateTimeFormatter.ofPattern("yyMMdd"));
        return String.format("TR%s%04d", datePart, nextSeq);
    }

    public void logTransfer(String itemCode, String itemName, Integer quantity,
                            Long sourceWarehouseId, String sourceWarehouseName,
                            Long targetWarehouseId, String targetWarehouseName,
                            String transferType, String referenceType, Long referenceId,
                            Long companyId, Long branchId, String transferredBy) {

        String trNo = generateTrNo();

        WarehouseTransferLog log = WarehouseTransferLog.builder()
                .trNo(trNo)
                .itemCode(itemCode)
                .itemName(itemName)
                .quantity(quantity)
                .sourceWarehouseId(sourceWarehouseId)
                .sourceWarehouseName(sourceWarehouseName)
                .targetWarehouseId(targetWarehouseId)
                .targetWarehouseName(targetWarehouseName)
                .transferType(transferType)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .companyId(companyId)
                .branchId(branchId)
                .transferredBy(transferredBy)
                .transferredAt(LocalDateTime.now())
                .build();

        transferLogRepository.save(log);
    }

    public List<WarehouseTransferLogDTO> getTransferLogsByItemAndSourceWarehouse(
            Long companyId,
            Long branchId,
            String itemCode,
            Long sourceWarehouseId
    ) {
        List<WarehouseTransferLog> logs = transferLogRepository.findByItemAndSourceWarehouse(
                companyId,
                branchId,
                itemCode,
                sourceWarehouseId
        );

        return logs.stream().map(log -> {
            WarehouseTransferLogDTO dto = new WarehouseTransferLogDTO();
            dto.setTrNo(log.getTrNo());
            dto.setItemCode(log.getItemCode());
            dto.setItemName(log.getItemName());
            dto.setQuantity(log.getQuantity());
            dto.setSourceWarehouseId(log.getSourceWarehouseId());
            dto.setSourceWarehouseName(log.getSourceWarehouseName());
            dto.setTargetWarehouseId(log.getTargetWarehouseId());
            dto.setTargetWarehouseName(log.getTargetWarehouseName());
            dto.setTransferType(log.getTransferType());
            dto.setReferenceType(log.getReferenceType());
            dto.setReferenceId(log.getReferenceId());
            dto.setTransferredBy(log.getTransferredBy());
            dto.setTransferredAt(log.getTransferredAt());
            return dto;
        }).toList();
    }

}
