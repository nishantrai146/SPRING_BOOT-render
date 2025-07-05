package com.lit.ims.service;

import com.lit.ims.dto.MaterialReceiptDTO;
import com.lit.ims.dto.MaterialReceiptItemDTO;
import com.lit.ims.entity.MaterialReceipt;
import com.lit.ims.entity.MaterialReceiptItem;
import com.lit.ims.repository.MaterialReceiptRepository;
import com.lit.ims.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaterialReceiptService {

    private final MaterialReceiptRepository receiptRepo;
    private final TransactionLogService logService;

    // DTO → Entity
    private MaterialReceipt toEntity(MaterialReceiptDTO dto, Long companyId, Long branchId) {
        MaterialReceipt receipt = MaterialReceipt.builder()
                .mode(dto.getMode())
                .vendor(dto.getVendor())
                .vendorCode(dto.getVendorCode())
                .companyId(companyId)
                .branchId(branchId)
                .build();

        List<MaterialReceiptItem> items = dto.getItems().stream().map(item ->
                MaterialReceiptItem.builder()
                        .itemName(item.getItemName())
                        .itemCode(item.getItemCode())
                        .quantity(item.getQuantity())
                        .batchNo(item.getBatchNo())
                        .receipt(receipt)
                        .build()
        ).collect(Collectors.toList());

        receipt.setItems(items);
        return receipt;
    }

    // Entity → DTO
    private MaterialReceiptDTO toDTO(MaterialReceipt receipt) {
        MaterialReceiptDTO dto = new MaterialReceiptDTO();
        dto.setMode(receipt.getMode());
        dto.setVendor(receipt.getVendor());
        dto.setVendorCode(receipt.getVendorCode());
        dto.setItems(receipt.getItems().stream().map(item -> {
            MaterialReceiptItemDTO i = new MaterialReceiptItemDTO();
            i.setItemName(item.getItemName());
            i.setItemCode(item.getItemCode());
            i.setQuantity(item.getQuantity());
            i.setBatchNo(item.getBatchNo());
            return i;
        }).collect(Collectors.toList()));
        return dto;
    }

    @Transactional
    public ApiResponse<MaterialReceiptDTO> saveReceipt(MaterialReceiptDTO dto, Long companyId, Long branchId) {
        try {
            MaterialReceipt saved = receiptRepo.save(toEntity(dto, companyId, branchId));
            logService.log("CREATE", "MaterialReceipt", saved.getId(), "Created receipt for vendor " + saved.getVendor());
            return new ApiResponse<>(true, "Material Receipt saved successfully", toDTO(saved));
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Batch number already exists. Please regenerate and try again.");
        }

    }


    public ApiResponse<List<MaterialReceiptDTO>> getAll(Long companyId, Long branchId) {
        List<MaterialReceiptDTO> list = receiptRepo.findAll().stream()
                .filter(r -> r.getCompanyId().equals(companyId) && r.getBranchId().equals(branchId))
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new ApiResponse<>(true, "Receipts fetched successfully", list);
    }

    public String generateBatchNumber(String vendorCode, String itemCode,String quantity, Long companyId, Long branchId) {
        String prefix = "M" + vendorCode + itemCode + quantity+
                LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));

        String last = receiptRepo.findMaxBatchNoWithPrefix(prefix);
        int next = 1;
        if (last != null && last.length() >= prefix.length() + 5) {
            try {
                next = Integer.parseInt(last.substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {}
        }
        return prefix + String.format("%05d", next);
    }
}
