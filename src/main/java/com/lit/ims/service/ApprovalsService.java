package com.lit.ims.service;

import com.lit.ims.dto.ApprovalsDTO;
import com.lit.ims.dto.StockAdjustmentApprovalDTO;
import com.lit.ims.entity.*;
import com.lit.ims.enums.ApprovalStage;
import com.lit.ims.enums.ApprovalStatus;
import com.lit.ims.enums.ReferenceType;
import com.lit.ims.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.lit.ims.enums.ReferenceType.WIP_RETURN;

@Service
@RequiredArgsConstructor
public class ApprovalsService {

    private final ApprovalsRepository repository;
    private final MaterialRequisitionRepository materialRequisitionRepository;
    private final UserRepository userRepository;
    private final ApprovalsRepository approvalsRepository;
    private final MaterialReceiptItemRepository materialReceiptItemRepository;
    private final WipReturnRepository wipReturnRepository;
    private final MaterialReceiptRepository materialReceiptRepository;
    private final InventoryStockService inventoryStockService;
    private final WarehouseRepository warehouseRepository;


    public void requestApproval(ReferenceType referenceType, Long referenceId, String requestedBy, String requestedTo, Long companyId, Long branchId,String metaData) {
        Approvals approval = Approvals.builder()
                .referenceType(referenceType)
                .referenceId(referenceId)
                .requestedBy(requestedBy)
                .requestedTo(requestedTo)
                .status(ApprovalStatus.PENDING)
                .companyId(companyId)
                .branchId(branchId)
                .requestedDate(LocalDateTime.now())
                .metaData(metaData)
                .build();
        repository.save(approval);
    }

//    public List<ApprovalsDTO> getMyApprovals(String username, Long companyId, Long branchId) {
//        return repository.findByRequestedToAndCompanyIdAndBranchId(username, companyId, branchId)
//                .stream()
//                .map(this::toDTO)
//                .collect(Collectors.toList());
//    }

    public void takeAction(Long approvalId, ApprovalStatus status, String remarks, String currentUsername) {
        Approvals approval = repository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval not found"));

        // Fetch current user details (to check role)
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only requested user or an ADMIN (e.g., OWNER) can approve
        boolean isApprover = approval.getRequestedTo().equalsIgnoreCase(currentUsername);
        boolean isAdminOrOwner = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.OWNER;

        if (!isApprover && !isAdminOrOwner) {
            throw new RuntimeException("You are not authorized to take action on this approval.");
        }

        // Update approval entity
        approval.setStatus(status);
        approval.setRemarks(remarks);
        approval.setActionDate(LocalDateTime.now());
        repository.save(approval);

        // Handle each type
        switch (approval.getReferenceType()) {

            case MATERIAL_REQUISITION -> {
                materialRequisitionRepository.findById(approval.getReferenceId())
                        .ifPresent(req -> {
                            req.setApprovalStatus(status);
                            materialRequisitionRepository.save(req);
                        });
            }

            case MATERIAL_RECEIPT -> {
                if (status == ApprovalStatus.APPROVED) {
                    List<MaterialReceiptItem> items = materialReceiptItemRepository
                            .findByReceiptId(approval.getReferenceId());
                    for (MaterialReceiptItem item : items) {
                        item.setAdjustmentLocked(false); // release lock
                    }
                    materialReceiptItemRepository.saveAll(items);
                }
            }

            case STOCK_ADJUSTMENT -> {
                materialReceiptItemRepository.findById(approval.getReferenceId())
                        .ifPresent(item -> {
                            boolean isApproved = status == ApprovalStatus.APPROVED;
                            item.setAdjustmentRequest(false);
                            item.setAdjustmentLocked(!isApproved); // lock if rejected
                            materialReceiptItemRepository.save(item);
                        });
            }

            case WIP_RETURN -> {
                WipReturn wipReturn = wipReturnRepository.findById(approval.getReferenceId())
                        .orElseThrow(() -> new RuntimeException("WIP Return not found"));

                if (status == ApprovalStatus.REJECTED) {
                    wipReturn.setApprovalStatus(ApprovalStatus.REJECTED);
                    wipReturnRepository.save(wipReturn);
                    break;
                }

                if (approval.getStage() == ApprovalStage.PRODUCTION_HEAD) {
                    Approvals storeApproval = Approvals.builder()
                            .referenceType(WIP_RETURN)
                            .referenceId(wipReturn.getId())
                            .requestedBy(currentUsername)
                            .requestedTo(findApprover("STORE", Role.ADMIN, approval.getCompanyId(), approval.getBranchId()))
                            .status(ApprovalStatus.PENDING)
                            .companyId(approval.getCompanyId())
                            .branchId(approval.getBranchId())
                            .requestedDate(LocalDateTime.now())
                            .stage(ApprovalStage.STORE_HEAD)
                            .metaData(approval.getMetaData())
                            .build();
                    repository.save(storeApproval);
                } else if (approval.getStage() == ApprovalStage.STORE_HEAD) {
                    wipReturn.setApprovalStatus(ApprovalStatus.APPROVED);
                    wipReturnRepository.save(wipReturn);

                    // ✅ Create Material Receipt
                    MaterialReceipt receipt = MaterialReceipt.builder()
                            .mode("WIP_RETURN")
                            .vendor("Internal")
                            .vendorCode("INTERNAL")
                            .companyId(wipReturn.getCompanyId())
                            .branchId(wipReturn.getBranchId())
                            .build();

                    List<MaterialReceiptItem> receiptItems = new ArrayList<>();
                    for (WipReturnItem item : wipReturn.getReturnItems()) {
                        MaterialReceiptItem receiptItem = MaterialReceiptItem.builder()
                                .itemCode(item.getItemCode().toString())
                                .itemName(item.getItemName())
                                .quantity(item.getReturnQty())
                                .batchNo(item.getNewBatchNo()) // ✅ use existing batch number
                                .receipt(receipt)
                                .qcStatus("PENDING") // or your logic
                                .isIssued(false)
                                .warehouse(Warehouse.builder().id(wipReturn.getWarehouseId()).build())
                                .build();
                        receiptItems.add(receiptItem);
                    }

                    receipt.setItems(receiptItems);
                    materialReceiptRepository.save(receipt);

                }

            }

            default -> throw new RuntimeException("Unsupported reference type: " + approval.getReferenceType());
        }
    }

    private String findApprover(String department, Role role, Long companyId, Long branchId) {
        return userRepository.findFirstApproverIgnoreCase(role, department, companyId, branchId)
                .map(User::getUsername)
                .orElseThrow(() -> new RuntimeException(
                        "No approver found for department: " + department + ", role: " + role));
    }



    private ApprovalsDTO toDTO(Approvals a) {
        return ApprovalsDTO.builder()
                .id(a.getId())
                .referenceType(a.getReferenceType())
                .referenceId(a.getReferenceId())
                .requestedBy(a.getRequestedBy())
                .requestedTo(a.getRequestedTo())
                .status(a.getStatus())
                .remarks(a.getRemarks())
                .requestedDate(a.getRequestedDate())
                .actionDate(a.getActionDate())
                .build();
    }
    public List<ApprovalsDTO> getMyApprovals(String username, Long companyId, Long branchId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Approvals> approvals;

        if (user.getRole() == Role.OWNER) {
            approvals = approvalsRepository.findByCompanyIdAndBranchId(companyId, branchId);
        } else {
            approvals = approvalsRepository.findByRequestedToAndCompanyIdAndBranchId(username, companyId, branchId);
        }

        return approvals.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ApprovalsDTO convertToDto(Approvals approvals) {
        return ApprovalsDTO.builder()
                .id(approvals.getId())
                .referenceType(approvals.getReferenceType())
                .referenceId(approvals.getReferenceId())
                .requestedBy(approvals.getRequestedBy())
                .requestedTo(approvals.getRequestedTo())
                .status(approvals.getStatus())
                .remarks(approvals.getRemarks())
                .requestedDate(approvals.getRequestedDate())
                .actionDate(approvals.getActionDate())
                .metaData(approvals.getMetaData())
                .build();
    }
    public void createStockAdjustmentApproval(StockAdjustmentApprovalDTO dto, String requestedBy, Long companyId, Long branchId) {
        MaterialReceiptItem item = materialReceiptItemRepository
                .findByBatchNoAndReceipt_CompanyIdAndReceipt_BranchId(dto.getBatchNo(), companyId, branchId)
                .orElseThrow(() -> new RuntimeException("Batch not found: " + dto.getBatchNo()));

        // Prevent duplicate requests
        if (Boolean.TRUE.equals(item.getAdjustmentRequest())) {
            throw new RuntimeException("An adjustment request is already pending for this item.");
        }

        // Lock the item and update with adjustment info
        item.setAdjustmentRequest(true);
        item.setAdjustedQuantity(dto.getRequestedQty());
        item.setAdjustmentReason(dto.getReason());
        item.setAdjustmentLocked(true);
        materialReceiptItemRepository.saveAndFlush(item);

        // 🔍 Build detailed metadata
        String metaData = String.format(
                "BatchNo: %s, ItemCode: %s, ItemName: %s, Received Qty: %s, Requested Adjustment Qty: %s, Warehouse: %s",
                item.getBatchNo() != null ? item.getBatchNo() : "N/A",
                item.getItemCode() != null ? item.getItemCode().toString() : "N/A",
                item.getItemName() != null ? item.getItemName() : "N/A",
                item.getQuantity() != null ? item.getQuantity().toString() : "N/A",
                dto.getRequestedQty() != null ? dto.getRequestedQty().toString() : "N/A",
                item.getWarehouse() != null ? item.getWarehouse().getName() : "N/A"
        );

        // Save approval entry
        Approvals approval = Approvals.builder()
                .referenceType(ReferenceType.STOCK_ADJUSTMENT)
                .referenceId(item.getId())
                .requestedBy(requestedBy)
                .requestedTo(findApproverForStockAdjustment(companyId, branchId))
                .companyId(companyId)
                .branchId(branchId)
                .status(ApprovalStatus.PENDING)
                .remarks(dto.getReason())
                .requestedDate(LocalDateTime.now())
                .metaData(metaData)
                .build();

        repository.save(approval);
    }

    private String findApproverForStockAdjustment(Long companyId, Long branchId) {
        return userRepository.findFirstApprover(
                        Role.ADMIN, "store", companyId, branchId)
                .map(User::getUsername)
                .orElseThrow(() -> new RuntimeException("No approver (ADMIN for INVENTORY) found"));
    }



}
