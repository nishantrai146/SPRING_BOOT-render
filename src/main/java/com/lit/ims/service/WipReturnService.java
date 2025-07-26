package com.lit.ims.service;

import com.lit.ims.dto.WipReturnDTO;
import com.lit.ims.dto.WipReturnItemDTO;
import com.lit.ims.entity.WipReturn;
import com.lit.ims.entity.WipReturnItem;
import com.lit.ims.repository.WipReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WipReturnService {

    private final WipReturnRepository wipReturnRepository;

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
                .map(i -> WipReturnItem.builder()
                        .itemId(i.getItemId())
                        .itemName(i.getItemName())
                        .batchNo(i.getBatchNo())
                        .originalQty(i.getOriginalQty())
                        .returnQty(i.getReturnQty())
                        .returnReason(i.getReturnReason())
                        .wipReturn(wipReturn)
                        .build())
                .collect(Collectors.toList());

        wipReturn.setReturnItems(items);
        return wipReturnRepository.save(wipReturn);
    }
}
