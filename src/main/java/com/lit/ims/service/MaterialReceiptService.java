package com.lit.ims.service;

import com.lit.ims.dto.*;
import com.lit.ims.entity.*;
import com.lit.ims.enums.ApprovalStatus;
import com.lit.ims.enums.ReferenceType;
import com.lit.ims.exception.ResourceNotFoundException;
import com.lit.ims.repository.*;
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
import java.util.*;
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
    private final WarehouseRepository warehouseRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final BatchSequenceTrackerRepository batchSequenceTrackerRepository;
    private final StockTransactionLogService stockTransactionLogService;
    private final InventoryStockService inventoryStockService;
    private final WarehouseTransferLogService warehouseTransferLogService;
    private final ApprovalsRepository approvalsRepository;
    private final UserRepository userRepository;

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
        List<MaterialReceiptItemDTO> itemDTOs = receipt.getItems().stream().map(item -> {
            MaterialReceiptItemDTO.MaterialReceiptItemDTOBuilder builder = MaterialReceiptItemDTO.builder()
                    .itemCode(item.getItemCode())
                    .itemName(item.getItemName())
                    .quantity(item.getQuantity())
                    .batchNo(item.getBatchNo())
                    .isIssued(item.isIssued())
                    .isInventory(item.getWarehouse() != null)
                    .warehouseId(item.getWarehouse() != null ? item.getWarehouse().getId() : null);

            return builder.build();
        }).toList();

        return MaterialReceiptDTO.builder()
                .vendor(receipt.getVendor())
                .vendorCode(receipt.getVendorCode())
                .mode(receipt.getMode())
                .items(itemDTOs)
                .build();
    }

    @Transactional
    public ApiResponse<MaterialReceiptDTO> saveReceipt(MaterialReceiptDTO dto, Long companyId, Long branchId) {
        try {
            log.info("Saving Material Receipt for vendor: {}, companyId: {}, branchId: {}", dto.getVendor(), companyId, branchId);

            // Create receipt header
            MaterialReceipt receipt = MaterialReceipt.builder()
                    .vendor(dto.getVendor())
                    .vendorCode(dto.getVendorCode())
                    .mode(dto.getMode())
                    .companyId(companyId)
                    .branchId(branchId)
                    .build();

            List<MaterialReceiptItem> items = new ArrayList<>();

            for (MaterialReceiptItemDTO itemDTO : dto.getItems()) {
                // Load Item Master
                Item itemMaster = itemRepository.findByCodeAndCompanyIdAndBranchId(
                        itemDTO.getItemCode(), companyId, branchId
                ).orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemDTO.getItemCode()));

                // Build item
                MaterialReceiptItem.MaterialReceiptItemBuilder itemBuilder = MaterialReceiptItem.builder()
                        .itemCode(itemDTO.getItemCode())
                        .itemName(itemMaster.getName())
                        .quantity(itemDTO.getQuantity())
                        .receipt(receipt)
                        .isIssued(false);

                // ✅ If inventory item, validate and set batchNo, warehouse, qcStatus, and update inventory
                if (itemMaster.isInventoryItem()) {
                    if (itemDTO.getBatchNo() == null || itemDTO.getBatchNo().isBlank()) {
                        throw new IllegalArgumentException("Batch number is required for inventory item: " + itemDTO.getItemCode());
                    }

                    if (materialReceiptItemRepository.existsByBatchNoAndReceipt_CompanyIdAndReceipt_BranchId(
                            itemDTO.getBatchNo(), companyId, branchId)) {
                        throw new RuntimeException("Batch number already exists. Please regenerate and try again.");
                    }
                    if (itemDTO.getWarehouseId() == null) {
                        throw new IllegalArgumentException("Warehouse is required for inventory item: " + itemDTO.getItemCode());
                    }

                    Warehouse warehouse = warehouseRepository.findById(itemDTO.getWarehouseId())
                            .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));

                    itemBuilder
                            .batchNo(itemDTO.getBatchNo())
                            .warehouse(warehouse)
                            .qcStatus(itemMaster.isIqc() ? "PENDING" : "PASS");

                    // Update InventoryStock
                    InventoryStock stock = inventoryStockRepository
                            .findByItemCodeAndWarehouse(itemDTO.getItemCode(), warehouse)
                            .orElseGet(() -> InventoryStock.builder()
                                    .itemCode(itemDTO.getItemCode())
                                    .itemName(itemMaster.getName())
                                    .warehouse(warehouse)
                                    .companyId(companyId)
                                    .branchId(branchId)
                                    .quantity(0)
                                    .build());

                    stock.setQuantity(stock.getQuantity() + itemDTO.getQuantity());
                    inventoryStockRepository.save(stock);
                    stockTransactionLogService.logTransaction(
                            itemDTO.getItemCode(),
                            itemMaster.getName(),
                            itemDTO.getQuantity(),
                            "INCREASE",
                            "MaterialReceipt",
                            null,
                            companyId,
                            branchId,
                            warehouse,
                            "Added via Material Receipt"
                    );
                } else {
                    // ❌ Non-inventory item: no batch, no warehouse, no qcStatus
                    itemBuilder
                            .batchNo(null)
                            .warehouse(null)
                            .qcStatus(null);
                }

                items.add(itemBuilder.build());
            }

            receipt.setItems(items);
            MaterialReceipt saved = receiptRepo.save(receipt);

            logService.log("CREATE", "MaterialReceipt", saved.getId(),
                    "Created Material Receipt for vendor " + saved.getVendor());

            return new ApiResponse<>(true, "Material Receipt saved successfully", toDTO(saved));

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity error: {}", e.getMessage());
            throw new RuntimeException("Batch number already exists. Please regenerate and try again.");
        } catch (Exception e) {
            log.error("Error while saving Material Receipt", e);
            throw new RuntimeException("Failed to save Material Receipt");
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
        Item item = itemRepository.findByCodeAndCompanyIdAndBranchId(itemCode, companyId, branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemCode));

        if (!item.isInventoryItem()) {
            // Non-inventory → no batch number required, return blank string to keep frontend happy
            return "";
        }

        String prefix = "M" + vendorCode + itemCode + quantity
                +LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));

        BatchSequenceTracker tracker = batchSequenceTrackerRepository
                .findByBatchPrefixAndCompanyIdAndBranchId(prefix, companyId, branchId)
                .orElse(null);

        if (tracker == null) {
            tracker = new BatchSequenceTracker();
            tracker.setBatchPrefix(prefix);
            tracker.setLastSequence(0);
            tracker.setCompanyId(companyId);
            tracker.setBranchId(branchId);
        }

        int nextSequence = tracker.getLastSequence() + 1;
        tracker.setLastSequence(nextSequence);
        tracker = batchSequenceTrackerRepository.save(tracker);

        return prefix + String.format("%05d", nextSequence);
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

            Optional<Item> optionalItem = itemRepository.findByCodeAndCompanyIdAndBranchId(itemCode, companyId, branchId);
            boolean isInventory = optionalItem.map(Item::isInventoryItem).orElse(false);
            boolean isIqc = optionalItem.map(Item::isIqc).orElse(false);

            /* 5. Build DTO straight from VendorItemsMaster */
            MaterialReceiptItemDTO dto = MaterialReceiptItemDTO.builder()
                    .batchNo(batchNo)
                    .vendorCode(vendorCode)
                    .vendorName(master.getVendorName())           // assumes column exists
                    .itemCode(master.getItemCode())
                    .itemName(master.getItemName())
                    .quantity(master.getQuantity())
                    .isInventory(isInventory)
                    .isIqc(isIqc)
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

    public void updateQcStatus(UpdateQcStatusDTO dto, Long companyId, Long branchId, String username) {
        MaterialReceiptItem item = materialReceiptItemRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (!item.getReceipt().getCompanyId().equals(companyId) || !item.getReceipt().getBranchId().equals(branchId)) {
            throw new RuntimeException("Access denied for company/branch");
        }

        // ➤ Get source (IQC) warehouse before updating the item
        Warehouse iqcWarehouse = warehouseRepository.findById(item.getWarehouse().getId())
                .orElseThrow(() -> new RuntimeException("IQC warehouse not found"));

        // ➤ Get target warehouse from DTO
        Warehouse targetWarehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Target warehouse not found"));

        // ➤ Set IQC status and update item details
        item.setQcStatus(dto.getQcStatus());
        item.setDefectCategory(dto.getDefectCategory());
        item.setRemarks(dto.getRemarks());
        item.setWarehouse(targetWarehouse); // ✅ Set new warehouse after QC pass/reject
        materialReceiptItemRepository.save(item);

        Integer quantity = item.getQuantity();
        String itemCode = item.getItemCode();
        String itemName = item.getItemName();

        // ➤ Update target InventoryStock
        inventoryStockService.addStock(
                itemCode,
                itemName,
                targetWarehouse.getId(),
                quantity,
                companyId,
                branchId
        );

        // ➤ Deduct from IQC InventoryStock
        inventoryStockService.removeStock(
                itemCode,
                iqcWarehouse.getId(),
                quantity,
                companyId,
                branchId
        );

        // ➤ Log Warehouse Transfer
        warehouseTransferLogService.logTransfer(
                itemCode,
                itemName,
                quantity,
                iqcWarehouse.getId(),
                iqcWarehouse.getName(),
                targetWarehouse.getId(),
                targetWarehouse.getName(),
                "QC_" + dto.getQcStatus().toUpperCase(),
                "MaterialReceiptItem",
                item.getId(),
                companyId,
                branchId,
                username
        );

        // ➤ Log Stock Transaction
        stockTransactionLogService.logTransaction(
                itemCode,
                itemName,
                quantity,
                "IQC_" + dto.getQcStatus().toUpperCase(),
                "MaterialReceiptItem",
                item.getId(),
                companyId,
                branchId,
                targetWarehouse,
                dto.getRemarks() != null ? dto.getRemarks() : "IQC Status Updated"
        );
    }

    public ApiResponse<PendingQcItemsDTO> getItemByBatchNo(String batchNo, Long companyId, Long branchId) {
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

            if (actualItem.isAdjustmentLocked()) {
                return new ApiResponse<>(false,
                        "Batch is locked pending admin approval of a quantity adjustment", null);
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

    public void raiseAdjustmentRequest(AdjustmentRequestDTO dto, Long companyId, Long branchId, String username) {
        String itemCode = dto.getItemCode();

        // 1. Find MaterialReceiptItem
        MaterialReceiptItem item = materialReceiptItemRepository
                .findByItemCodeAndReceipt_CompanyIdAndReceipt_BranchId(itemCode, companyId, branchId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        // 2. Prevent duplicate requests
        if (Boolean.TRUE.equals(item.getAdjustmentRequest())) {
            throw new RuntimeException("An adjustment request is already pending for this item.");
        }

        // 3. Update item with adjustment details and lock
        item.setAdjustmentRequest(true);
        item.setAdjustedQuantity(dto.getAdjustedQuantity());
        item.setAdjustmentReason(dto.getReason());
        item.setAdjustmentLocked(true); // 🟢 Ensure locking is applied

        materialReceiptItemRepository.saveAndFlush(item);

        // 4. Build metadata safely
        String metaData = String.format(
                "BatchNo: %s, Requested Qty: %s",
                item.getBatchNo() != null ? item.getBatchNo() : "N/A",
                dto.getAdjustedQuantity() != null ? dto.getAdjustedQuantity() : "N/A"
        );

        // 5. Create approval request
        Approvals approval = Approvals.builder()
                .companyId(companyId)
                .branchId(branchId)
                .referenceType(ReferenceType.STOCK_ADJUSTMENT)
                .referenceId(item.getId())
                .requestedBy(username)
                .requestedTo(findApprover("store"))
                .status(ApprovalStatus.PENDING)
                .remarks(dto.getReason())
                .metaData(metaData)
                .build();

        approvalsRepository.save(approval);
    }

    private String findApprover(String department) {
        return userRepository.findFirstByRoleAndDepartment(Role.ADMIN, "store")
                .map(User::getUsername)
                .orElseThrow(() -> new RuntimeException("No approver found for department: " + department));
    }
    public Map<String, Object> getItemsByDate(LocalDate date, Long companyId, Long branchId) {
        List<MaterialReceiptItem> items = materialReceiptItemRepository
                .findByReceiptDateAndCompanyIdAndBranchId(date, companyId, branchId);

        List<MaterialReceiptItemInfoDTO> dtoList = items.stream().map(item -> {
            MaterialReceipt receipt = item.getReceipt();  // from MaterialReceiptItem
            return new MaterialReceiptItemInfoDTO(
                    receipt.getVendorCode(),
                    receipt.getVendor(),
                    item.getItemCode(),
                    item.getItemName()
            );
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("items", dtoList);
        result.put("count", dtoList.size());
        return result;
    }



}
