package com.lit.ims.service;

import com.lit.ims.entity.WarehouseTransferLog;
import com.lit.ims.repository.WarehouseTransferLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WarehouseTransferLogService {
    private final WarehouseTransferLogRepository transferLogRepository;

    public void logTransfer(String itemCode, String itemName, Integer quantity,
                            Long sourceWarehouseId, String sourceWarehouseName,
                            Long targetWarehouseId, String targetWarehouseName,
                            String transferType, String referenceType, Long referenceId,
                            Long companyId, Long branchId, String transferredBy) {

        WarehouseTransferLog log = WarehouseTransferLog.builder()
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
}
