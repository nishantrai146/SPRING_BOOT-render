package com.lit.ims.service;

import com.lit.ims.dto.MaterialReceiptDTO;
import com.lit.ims.dto.MaterialReceiptItemDTO;
import com.lit.ims.dto.PendingQcItemsDTO;
import com.lit.ims.dto.UpdateQcStatusDTO;
import com.lit.ims.entity.MaterialReceipt;
import com.lit.ims.entity.MaterialReceiptItem;
import com.lit.ims.entity.VendorItemsMaster;
import com.lit.ims.exception.ResourceNotFoundException;
import com.lit.ims.repository.MaterialReceiptItemRepository;
import com.lit.ims.repository.MaterialReceiptRepository;
import com.lit.ims.repository.VendorItemsMasterRepository;
import com.lit.ims.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialReceiptService {

    private final MaterialReceiptRepository receiptRepo;
    private final VendorItemsMasterRepository vendorItemsRepo;
    private final TransactionLogService logService;
    private final MaterialReceiptItemRepository materialReceiptItemRepository;

    // DTO → Entity
    private MaterialReceipt toEntity(MaterialReceiptDTO dto, Long companyId, Long branchId) {
        log.info("Converting DTO to Entity. DTO: {}", dto);
        MaterialReceipt receipt = MaterialReceipt.builder()
                .mode(dto.getMode())
                .vendor(dto.getVendor())
                .vendorCode(dto.getVendorCode())
                .companyId(companyId)
                .branchId(branchId)
                .build();

        List<MaterialReceiptItem> items = dto.getItems().stream().map(item -> {
            if (item.getBatchNo() == null || item.getBatchNo().isBlank()) {
                throw new RuntimeException("Batch number is missing for item: " + item.getItemName());
            }

            log.info("Mapping Item DTO to Entity: {}", item);

            return MaterialReceiptItem.builder()
                    .itemName(item.getItemName())
                    .itemCode(item.getItemCode())
                    .quantity(item.getQuantity())
                    .batchNo(item.getBatchNo())
                    .qcStatus("PENDING")
                    .receipt(receipt)
                    .build();
        }).collect(Collectors.toList());

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
            log.info("Saving Material Receipt with vendor: {}, companyId: {}, branchId: {}", dto.getVendor(), companyId, branchId);

            // Log incoming items
            if (dto.getItems() != null) {
                dto.getItems().forEach(item -> log.info("Received Item: {}", item));
            } else {
                log.warn("No items found in the DTO");
            }

            MaterialReceipt saved = receiptRepo.save(toEntity(dto, companyId, branchId));

            logService.log("CREATE", "MaterialReceipt", saved.getId(), "Created receipt for vendor " + saved.getVendor());

            return new ApiResponse<>(true, "Material Receipt saved successfully", toDTO(saved));
        } catch (DataIntegrityViolationException e) {
            log.error("Error saving receipt - duplicate batch number: {}", e.getMessage());
            throw new RuntimeException("Batch number already exists. Please regenerate and try again.");
        } catch (Exception e) {
            log.error("Unexpected error while saving material receipt", e);
            throw new RuntimeException("An error occurred while saving receipt: " + e.getMessage());
        }
    }
    @Transactional
    public ApiResponse<List<MaterialReceiptDTO>> getAll(Long companyId, Long branchId) {
        List<MaterialReceiptDTO> list = receiptRepo.findAll().stream()
                .filter(r -> r.getCompanyId().equals(companyId) && r.getBranchId().equals(branchId))
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new ApiResponse<>(true, "Receipts fetched successfully", list);
    }
    @Transactional
    public String generateBatchNumber(String vendorCode, String itemCode, String quantity, Long companyId, Long branchId) {
        String prefix = "M" + vendorCode + itemCode + quantity +
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

    public ApiResponse<MaterialReceiptItemDTO> verifyBatchNumber(String batchNo, Long companyId, Long branchId) {
        try {
            if (batchNo.length() < 28) {
                return new ApiResponse<>(false, "Invalid batch number format", null);
            }

            String vendorCode = batchNo.substring(1, 7);       // 6 chars
            String itemCode = batchNo.substring(7, 13);        // 6 chars

            List<VendorItemsMaster> vendorItems = vendorItemsRepo
                    .findByVendorCodeAndCompanyIdAndBranchId(vendorCode, companyId, branchId);

            if (vendorItems.isEmpty()) {
                return new ApiResponse<>(false, "Invalid Vendor Code: " + vendorCode, null);
            }

            Optional<VendorItemsMaster> match = vendorItems.stream()
                    .filter(item -> item.getItemCode().equals(itemCode))
                    .findFirst();

            if (match.isEmpty()) {
                return new ApiResponse<>(false, "Item Code " + itemCode + " not found under Vendor " + vendorCode, null);
            }

            VendorItemsMaster master = match.get();

            MaterialReceiptItemDTO dto = new MaterialReceiptItemDTO();
            dto.setItemCode(master.getItemCode());
            dto.setItemName(master.getItemName());
            dto.setQuantity(master.getQuantity());
            dto.setBatchNo(batchNo);

            return new ApiResponse<>(true, "Batch number verified", dto);

        } catch (Exception e) {
            log.error("Error verifying batch number", e);
            return new ApiResponse<>(false, "Error verifying batch number: " + e.getMessage(), null);
        }
    }

    public ApiResponse<List<PendingQcItemsDTO>> getPendingQcItems(Long companyId, Long branchId){
        List<MaterialReceiptItem> items=materialReceiptItemRepository.findByQcStatusAndReceipt_CompanyIdAndReceipt_BranchId("PENDING",companyId,branchId);

        List<PendingQcItemsDTO> result=items.stream().map(item ->{
            PendingQcItemsDTO dto=new PendingQcItemsDTO();
            dto.setId(item.getId());
            dto.setItemName(item.getItemName());
            dto.setItemCode(item.getItemCode());
            dto.setQuantity(item.getQuantity());
            dto.setBatchNumber(item.getBatchNo());
            dto.setVendorName(item.getReceipt().getVendor());
            dto.setVendorCode(item.getReceipt().getVendorCode());
            dto.setCreatedAt(item.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());

        return new ApiResponse<>(true,"Pending Qc items fetched Successfully",result);
    }

    public ApiResponse<List<PendingQcItemsDTO>> getItemsWithPassOrFail(Long companyId,Long branchId){
        List<String> statusList=List.of("PASS","FAIL","pass","fail");

        List<MaterialReceiptItem> items=materialReceiptItemRepository.findByQcStatusInAndReceipt_CompanyIdAndReceipt_BranchId(statusList,companyId,branchId);

        List<PendingQcItemsDTO> dtoList=items.stream().map(item->{
            PendingQcItemsDTO dto=new PendingQcItemsDTO();
            dto.setId(item.getId());
            dto.setItemCode(item.getItemCode());
            dto.setItemName(item.getItemName());
            dto.setBatchNumber(item.getBatchNo());
            dto.setVendorName(item.getReceipt().getVendor());
            dto.setVendorCode(item.getReceipt().getVendorCode());
            dto.setCreatedAt(item.getCreatedAt());
            dto.setQuantity(item.getQuantity());
            return dto;
        }).collect(Collectors.toList());

        return new ApiResponse<>(true,"Pass/Fail item from OQC",dtoList);

    }

    @Transactional
    public ApiResponse<String> updateQcStatus(UpdateQcStatusDTO dto, Long companyId, Long branchId) {
        Optional<MaterialReceiptItem> optionalItem = materialReceiptItemRepository
                .findByIdAndReceipt_CompanyIdAndReceipt_BranchId(dto.getId(), companyId, branchId);

        if (optionalItem.isEmpty()) {
            throw new ResourceNotFoundException("Material Receipt Item not found for this company/branch");
        }

        MaterialReceiptItem item = optionalItem.get();
        item.setQcStatus(dto.getQcStatus().toUpperCase());
        materialReceiptItemRepository.save(item);

        return new ApiResponse<>(true, "QC status updated successfully",null);
    }


}
