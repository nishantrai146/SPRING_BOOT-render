package com.lit.ims.service;

import com.lit.ims.dto.*;
import com.lit.ims.entity.Item;
import com.lit.ims.entity.MaterialReceipt;
import com.lit.ims.entity.MaterialReceiptItem;
import com.lit.ims.entity.VendorItemsMaster;
import com.lit.ims.repository.ItemRepository;
import com.lit.ims.repository.MaterialReceiptItemRepository;
import com.lit.ims.repository.MaterialReceiptRepository;
import com.lit.ims.repository.VendorItemsMasterRepository;
import com.lit.ims.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final TransactionLogService transactionLogService;
    private final ItemRepository itemRepository;

    private Integer fetchItemQuantity(String itemCode, Long companyId, Long branchId) {
        return itemRepository.findByCodeAndCompanyIdAndBranchId(itemCode, companyId, branchId)
                .map(item -> item.getStQty() != null ? item.getStQty() : 0)
                .orElse(0);
    }


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

        String last = receiptRepo.findMaxBatchNoWithPrefix(prefix, companyId, branchId);
        int next = 1;
        if (last != null && last.length() >= prefix.length() + 5) {
            try {
                next = Integer.parseInt(last.substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
            }
        }
        return prefix + String.format("%05d", next);
    }

    public ApiResponse<MaterialReceiptItemDTO> verifyBatchAndFetchDetails(
            String batchNo, Long companyId, Long branchId) {

        try {
            /* 1. Basic length check */
            if (batchNo == null || batchNo.length() < 28) {
                return new ApiResponse<>(false, "Invalid batch number format", null);
            }
            if (materialReceiptItemRepository
                    .existsByBatchNoAndReceipt_CompanyIdAndReceipt_BranchId(batchNo, companyId, branchId)) {
                return new ApiResponse<>(false,
                        "Batch number " + batchNo + " already exists in Material Receipt", null);
            }

            /* 2. Parse vendor & item codes from the barcode */
            String vendorCode = batchNo.substring(1, 7);   // chars 1‑6
            String itemCode   = batchNo.substring(7, 13);  // chars 7‑12

            /* 3. Fetch all vendor‑item mappings for this vendor in the current branch */
            List<VendorItemsMaster> vendorItems = vendorItemsRepo
                    .findByVendorCodeAndCompanyIdAndBranchId(vendorCode, companyId, branchId);

            if (vendorItems.isEmpty()) {
                return new ApiResponse<>(false,
                        "Invalid Vendor Code: " + vendorCode, null);
            }

            /* 4. Find the matching item code */
            VendorItemsMaster master = vendorItems.stream()
                    .filter(vim -> vim.getItemCode().equals(itemCode))
                    .findFirst()
                    .orElse(null);

            if (master == null) {
                return new ApiResponse<>(false,
                        "Item Code " + itemCode + " not found under Vendor " + vendorCode, null);
            }

            /* 5. Build DTO straight from VendorItemsMaster */
            MaterialReceiptItemDTO dto = MaterialReceiptItemDTO.builder()
                    .batchNo(batchNo)
                    .vendorCode(vendorCode)
                    .vendorName(master.getVendorName())           // assumes column exists
                    .itemCode(master.getItemCode())
                    .itemName(master.getItemName())
                    .quantity(master.getQuantity())               // or getStockQty(), etc.
                    .build();

            return new ApiResponse<>(true, "Batch verified", dto);

        } catch (Exception ex) {
            log.error("Error verifying batch number", ex);
            return new ApiResponse<>(false,
                    "Error verifying batch number: " + ex.getMessage(), null);
        }
    }

    public ApiResponse<List<PendingQcItemsDTO>> getPendingQcItems(Long companyId, Long branchId) {
        List<MaterialReceiptItem> items = materialReceiptItemRepository.findByQcStatusAndReceipt_CompanyIdAndReceipt_BranchId("PENDING", companyId, branchId);

        List<PendingQcItemsDTO> result = items.stream().map(item -> {
            PendingQcItemsDTO dto = new PendingQcItemsDTO();
            dto.setId(item.getId());
            dto.setItemName(item.getItemName());
            dto.setItemCode(item.getItemCode());
            dto.setQuantity(item.getQuantity());
            dto.setBatchNumber(item.getBatchNo());
            dto.setStatus(item.getQcStatus());
            dto.setVendorName(item.getReceipt().getVendor());
            dto.setVendorCode(item.getReceipt().getVendorCode());
            dto.setCreatedAt(item.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());

        return new ApiResponse<>(true, "Pending Qc items fetched Successfully", result);
    }

    public ApiResponse<List<PendingQcItemsDTO>> getItemsWithPassOrFail(Long companyId, Long branchId) {
        List<String> statusList = List.of("PASS", "FAIL", "pass", "fail");

        List<MaterialReceiptItem> items = materialReceiptItemRepository.findByQcStatusInAndReceipt_CompanyIdAndReceipt_BranchId(statusList, companyId, branchId);

        List<PendingQcItemsDTO> dtoList = items.stream().map(item -> {
            PendingQcItemsDTO dto = new PendingQcItemsDTO();
            dto.setId(item.getId());
            dto.setItemCode(item.getItemCode());
            dto.setItemName(item.getItemName());
            dto.setBatchNumber(item.getBatchNo());
            dto.setStatus(item.getQcStatus());
            dto.setVendorName(item.getReceipt().getVendor());
            dto.setVendorCode(item.getReceipt().getVendorCode());
            dto.setCreatedAt(item.getCreatedAt());
            dto.setQuantity(item.getQuantity());
            return dto;
        }).collect(Collectors.toList());

        return new ApiResponse<>(true, "Pass/Fail item from OQC", dtoList);

    }

    @Transactional
    public ApiResponse<String> updateQcStatus(UpdateQcStatusDTO dto, Long companyId, Long branchId) {
        MaterialReceiptItem item = materialReceiptItemRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Item not found"));

        item.setQcStatus(dto.getQcStatus().toUpperCase());

        if ("FAIL".equalsIgnoreCase(dto.getQcStatus())) {
            item.setDefectCategory(dto.getDefectCategory());
            item.setRemarks(dto.getRemarks());
        }

        materialReceiptItemRepository.save(item);

        logService.log("UPDATE", "MaterialReceiptItem", item.getId(),
                "QC Status updated to " + dto.getQcStatus());

        return new ApiResponse<>(true, "QC status updated successfully", null);
    }

    public ApiResponse<PendingQcItemsDTO> getitemByBatchNo(String batchNo, Long companyId, Long branchId) {
        MaterialReceiptItem item = materialReceiptItemRepository.findByBatchNoAndReceipt_CompanyIdAndReceipt_BranchId(batchNo, companyId, branchId)
                .orElseThrow(() -> new RuntimeException("Item not found for batch number" + batchNo));

        PendingQcItemsDTO dto = new PendingQcItemsDTO();
        dto.setId(item.getId());
        dto.setItemCode(item.getItemCode());
        dto.setItemName(item.getItemName());
        dto.setQuantity(item.getQuantity());
        dto.setStatus(item.getQcStatus());
        dto.setBatchNumber(item.getBatchNo());
        dto.setVendorCode(item.getReceipt().getVendorCode());
        dto.setVendorName(item.getReceipt().getVendor());
        dto.setCreatedAt(item.getCreatedAt());

        return new ApiResponse<>(true, "Item Fetched Successfully", dto);
    }

    public ApiResponse<String> deletePendingQc(Long id, Long companyId, Long branchId) {
        Optional<MaterialReceiptItem> optionalItem = materialReceiptItemRepository.findById(id);

        if (optionalItem.isEmpty()) {
            return new ApiResponse<>(false, "Transaction not found!", null);
        }

        MaterialReceiptItem item = optionalItem.get();

        if (!item.getReceipt().getCompanyId().equals(companyId) && !item.getReceipt().getBranchId().equals(branchId)) {
            return new ApiResponse<>(false, "Unauthorized access to this transaction.", null);
        }

        if (!"PENDING".equalsIgnoreCase(item.getQcStatus())) {
            return new ApiResponse<>(false, "Only transactions with PENDING QC status can be deleted.", null);
        }

        materialReceiptItemRepository.delete(item);

        logService.log("DELETE", "MaterialReceiptItem", id,
                "Deleted pending transaction with batchNo: " + item.getBatchNo());

        return new ApiResponse<>(true, "Material receipt Deleted Successfully", null);

    }

    @Transactional
    public ApiResponse<MaterialReceiptItemDTO> verifyBatchAndReserveIfFifo(
            String batchNo, Long companyId, Long branchId, String username) {

        try {
            /* ─── 0. Format check ─────────────────────────────── */
            if (batchNo == null || batchNo.length() < 28) {
                return new ApiResponse<>(false, "Invalid batch number format", null);
            }

            String vendorCode = batchNo.substring(1, 7);
            String itemCode = batchNo.substring(7, 13);

            /* ─── 1. Load batch row ───────────────────────────── */
            MaterialReceiptItem actualItem = materialReceiptItemRepository
                    .findByBatchNoAndReceipt_CompanyIdAndReceipt_BranchId(
                            batchNo, companyId, branchId)
                    .orElseThrow(() -> new RuntimeException("Batch not found: " + batchNo));

            if (actualItem.isIssued()) {
                return new ApiResponse<>(false, "Batch already issued: " + batchNo, null);
            }

            // 🔑 Allow if already reserved *by this user*; block if by someone else
            if (actualItem.getReservedBy() != null && !actualItem.getReservedBy().equals(username)) {
                return new ApiResponse<>(false, "Batch is already reserved by another user", null);
            }

            if (!"PASS".equalsIgnoreCase(actualItem.getQcStatus())) {
                return new ApiResponse<>(false, "QC status must be PASS. Found: "
                        + actualItem.getQcStatus(), null);
            }

            /* ─── 2. Shelf‑life check ─────────────────────────── */
            Item itemMaster = itemRepository
                    .findByCodeAndCompanyIdAndBranchId(itemCode, companyId, branchId)
                    .orElseThrow(() -> new RuntimeException("Item not found in Item Master: " + itemCode));

            int lifeInDays = itemMaster.getLife() == null ? 0 : itemMaster.getLife();
            if (actualItem.getCreatedAt() != null &&
                    LocalDate.now().isAfter(actualItem.getCreatedAt().toLocalDate().plusDays(lifeInDays))) {
                return new ApiResponse<>(false, "Shelf life exceeded for batch: " + batchNo, null);
            }

            /* ─── 3. FIFO validation ─────────────────────────── */
            List<MaterialReceiptItem> fifoCandidates =
                    materialReceiptItemRepository
                            .findAllByItemCodeAndReceipt_VendorCodeAndIsIssuedFalseAndQcStatusIgnoreCaseAndReceipt_CompanyIdAndReceipt_BranchIdOrderByCreatedAtAsc(
                                    itemCode, vendorCode, "PASS", companyId, branchId);

            // Earliest completely UN‑reserved, UN‑expired batch
            Optional<MaterialReceiptItem> earliestUnreserved = fifoCandidates.stream()
                    .filter(it -> it.getReservedBy() == null) // skip anything already reserved
                    .filter(it -> {
                        if (it.getCreatedAt() == null) return false;
                        LocalDate exp = it.getCreatedAt().toLocalDate().plusDays(lifeInDays);
                        return !LocalDate.now().isAfter(exp);
                    })
                    .findFirst();

            if (earliestUnreserved.isPresent()
                    && !earliestUnreserved.get().getBatchNo().equals(batchNo)) {
                return new ApiResponse<>(false,
                        "FIFO violation. Earliest un‑reserved batch is: "
                                + earliestUnreserved.get().getBatchNo(), null);
            }

            /* ─── 4. Reserve for current user ────────────────── */
            actualItem.setReservedBy(username);                 // idempotent if already same user
            actualItem.setReservedAt(LocalDateTime.now());
            materialReceiptItemRepository.save(actualItem);

            MaterialReceiptItemDTO dto = new MaterialReceiptItemDTO();
            dto.setItemCode(actualItem.getItemCode());
            dto.setItemName(actualItem.getItemName());
            dto.setQuantity(actualItem.getQuantity());
            dto.setBatchNo(actualItem.getBatchNo());

            logService.log("UPDATE", "MaterialReceiptItem", actualItem.getId(),
                    "Batch reserved for FIFO issue");

            return new ApiResponse<>(true, "Batch reserved successfully", dto);

        } catch (Exception e) {
            log.error("Error reserving batch for FIFO issue", e);
            return new ApiResponse<>(false, "Internal error: " + e.getMessage(), null);
        }
    }

    @Transactional
    public ApiResponse<String> confirmIssuedBatch(String batchNo, Long companyId, Long branchId, String username) {
        MaterialReceiptItem item = materialReceiptItemRepository
                .findByBatchNoAndReceipt_CompanyIdAndReceipt_BranchId(batchNo, companyId, branchId)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        if (!username.equals(item.getReservedBy())) {
            return new ApiResponse<>(false, "You are not authorized to confirm this batch", null);
        }

        item.setIssued(true);
        item.setReservedBy(null);
        item.setReservedAt(null);
        materialReceiptItemRepository.save(item);

        logService.log("UPDATE", "MaterialReceiptItem", item.getId(), "Batch confirmed as issued");
        return new ApiResponse<>(true, "Batch confirmed as issued", null);
    }


    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @Transactional
    public void releaseStaleReservations() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);
        List<MaterialReceiptItem> stale = materialReceiptItemRepository.findAllByReservedAtBeforeAndIsIssuedFalse(cutoff);

        for (MaterialReceiptItem item : stale) {
            item.setReservedBy(null);
            item.setReservedAt(null);
            materialReceiptItemRepository.save(item);

            logService.log("UPDATE", "MaterialReceiptItem", item.getId(), "Stale reservation auto-released");
        }
    }

    @Transactional
    public ApiResponse<String> releaseReservedBatch(String batchNo, Long companyId, Long branchId, String username) {
        try {
            MaterialReceiptItem item = materialReceiptItemRepository
                    .findByBatchNoAndReceipt_CompanyIdAndReceipt_BranchId(batchNo, companyId, branchId)
                    .orElseThrow(() -> new RuntimeException("Batch not found: " + batchNo));

            // Check if reserved and by whom
            if (item.getReservedBy() == null) {
                return new ApiResponse<>(false, "Batch is not reserved", null);
            }

            if (!item.getReservedBy().equals(username)) {
                return new ApiResponse<>(false, "Batch reserved by another user", null);
            }

            // Release the reservation
            item.setReservedBy(null);
            item.setReservedAt(null);
            materialReceiptItemRepository.save(item);

            logService.log("UPDATE", "MaterialReceiptItem", item.getId(), "Manual reservation released by user: " + username);

            return new ApiResponse<>(true, "Batch reservation released successfully", null);
        } catch (Exception e) {
            log.error("Error releasing reserved batch", e);
            return new ApiResponse<>(false, "Error: " + e.getMessage(), null);
        }
    }

    public ApiResponse<IqcStatusCountDTO> getIqcStatusCounts(Long companyId, Long branchId) {
        long pendingCount = materialReceiptItemRepository
                .countByQcStatusIgnoreCaseAndReceipt_CompanyIdAndReceipt_BranchId("PENDING", companyId, branchId);

        long passCount = materialReceiptItemRepository
                .countByQcStatusIgnoreCaseAndReceipt_CompanyIdAndReceipt_BranchId("PASS", companyId, branchId);

        long failCount = materialReceiptItemRepository
                .countByQcStatusIgnoreCaseAndReceipt_CompanyIdAndReceipt_BranchId("FAIL", companyId, branchId);

        IqcStatusCountDTO dto = new IqcStatusCountDTO(pendingCount, passCount, failCount);
        return new ApiResponse<>(true, "IQC status counts fetched successfully", dto);
    }


}
