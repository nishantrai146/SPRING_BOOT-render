package com.lit.ims.service;

import com.lit.ims.dto.StockTransactionLogDTO;
import com.lit.ims.dto.StockTransactionLogFilterDTO;
import com.lit.ims.entity.StockTransactionLog;
import com.lit.ims.entity.Warehouse;
import com.lit.ims.repository.StockTransactionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockTransactionLogService {

    private final StockTransactionLogRepository logRepository;

    public void save(StockTransactionLog log) {
        logRepository.save(log);
    }


    public void logTransaction(String itemCode, String itemName, int quantityChanged,
                               String transactionType, String referenceType, Long referenceId,
                               Long companyId, Long branchId, Warehouse warehouse, String remarks) {
        StockTransactionLog log = StockTransactionLog.builder()
                .itemCode(itemCode)
                .itemName(itemName)
                .transactionType(transactionType)
                .quantityChanged(quantityChanged)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .companyId(companyId)
                .branchId(branchId)
                .warehouse(warehouse)
                .transactionDate(LocalDateTime.now())
                .remarks(remarks)
                .build();

        logRepository.save(log);
    }

    public List<StockTransactionLogDTO> getLogsWithFilters(Long companyId, Long branchId, StockTransactionLogFilterDTO filter) {
        LocalDateTime from = (filter.getFromDate() != null) ? filter.getFromDate().atStartOfDay() : null;
        LocalDateTime to = (filter.getToDate() != null) ? filter.getToDate().atTime(23, 59, 59) : null;

        List<StockTransactionLog> logs = logRepository.findWithFilters(
                companyId,
                branchId,
                filter.getItemCode(),
                filter.getWarehouseId(),
                filter.getTransactionType(),
                from,
                to
        );

        return logs.stream().map(log -> {
            StockTransactionLogDTO dto = new StockTransactionLogDTO();
            dto.setItemCode(log.getItemCode());
            dto.setItemName(log.getItemName());
            dto.setQuantityChanged(log.getQuantityChanged());
            dto.setTransactionDate(log.getTransactionDate());
            dto.setTransactionType(log.getTransactionType());
            dto.setReferenceId(log.getReferenceId());
            dto.setReferenceType(log.getReferenceType());
            dto.setWarehouseId(log.getWarehouse().getId());
            dto.setWarehouseName(log.getWarehouse().getName());
            dto.setRemarks(log.getRemarks());
            return dto;
        }).toList();
    }

}
