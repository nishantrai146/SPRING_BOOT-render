package com.lit.ims.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lit.ims.dto.*;
import com.lit.ims.entity.*;
import com.lit.ims.enums.ApprovalStatus;
import com.lit.ims.enums.ReferenceType;
import com.lit.ims.exception.DuplicateResourceException;
import com.lit.ims.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialRequisitionService {

    private final MaterialRequisitionRepository repository;
    private final ItemRepository itemRepository;
    private final BomRepository bomRepository;
    private final BomItemRepository bomItemRepository;
    private final WarehouseRepository warehouseRepository;
    private final ApprovalsService approvalsService;
    private final UserRepository userRepository;



    @Transactional
    public MaterialRequisitions save(MaterialRequisitionDTO dto, Long companyId, Long branchId, String createdBy) {
        if (repository.existsByTransactionNumberAndCompanyIdAndBranchId(dto.getTransactionNumber(), companyId, branchId)) {
            throw new DuplicateResourceException("Transaction Number Already exists.");
        }

        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        MaterialRequisitions requisitions = MaterialRequisitions.builder()
                .transactionNumber(dto.getTransactionNumber())
                .type(dto.getType())
                .companyId(companyId)
                .branchId(branchId)
                .warehouse(warehouse)
                .items(new ArrayList<>())
                .build();

        if (dto.getItems() != null) {
            for (RequestedItemDTO itemDTO : dto.getItems()) {
                MaterialRequisitionItem item = MaterialRequisitionItem.builder()
                        .name(itemDTO.getName())
                        .code(itemDTO.getCode())
                        .quantity(itemDTO.getQuantity())
                        .type(itemDTO.getType())
                        .requisition(requisitions)
                        .build();
                requisitions.getItems().add(item);
            }
        }

        MaterialRequisitions saved = repository.save(requisitions);

        // ✅ Find approver: ADMIN of PRODUCTION
        String approverUsername = userRepository
                .findFirstByRoleAndDepartment(Role.ADMIN, "Production")
                .map(User::getUsername)
                .orElseThrow(() -> new RuntimeException("Approver not found for PRODUCTION department"));

        // ✅ Create metadata
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("Transaction Number", saved.getTransactionNumber());
        meta.put("Type", saved.getType());
        meta.put("Warehouse", warehouse.getName());
        meta.put("Requested Items", saved.getItems().stream().map(item ->
                Map.of(
                        "Code", item.getCode(),
                        "Name", item.getName(),
                        "Qty", item.getQuantity(),
                        "Type", item.getType()
                )
        ).collect(Collectors.toList()));

        String metaDataJson;
        try {
            metaDataJson = new ObjectMapper().writeValueAsString(meta);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize metadata", e);
        }

        // ✅ Create approval request with metadata
        approvalsService.requestApproval(
                ReferenceType.MATERIAL_REQUISITION,
                saved.getId(),
                createdBy,
                approverUsername,
                companyId,
                branchId,
                metaDataJson
        );

        log.info("Saved material requisition and triggered approval to {}", approverUsername);

        return saved;
    }

    public List<RequisitionSummaryDTO> getRecent(Long companyId, Long branchId) {
        return repository.findTop10ByCompanyIdAndBranchIdOrderByCreatedAtDesc(companyId, branchId)
                .stream()
                .map(r -> RequisitionSummaryDTO.builder()
                        .id(r.getId())
                        .transactionNumber(r.getTransactionNumber())
                        .type(r.getType())
                        .status(r.getStatus().name())
                        .createdAt(r.getCreatedAt())
                        .items(
                                r.getItems().stream()
                                        .map(item -> {
                                            RequestedItemDTO dto = new RequestedItemDTO();
                                            dto.setName(item.getName());
                                            dto.setCode(item.getCode());
                                            dto.setType(item.getType());
                                            dto.setQuantity(item.getQuantity());
                                            return dto;
                                        }).toList()
                        )
                        .build())
                .toList();
    }

    public void updateStatus(Long id, String status, Long companyId, Long branchId) {
        MaterialRequisitions requisition = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requisition not found"));

        if (!requisition.getCompanyId().equals(companyId) || !requisition.getBranchId().equals(branchId)) {
            throw new RuntimeException("Unauthorized to update requisition from another company/branch");
        }

        try {
            RequisitionStatus newStatus = RequisitionStatus.valueOf(status.toUpperCase());
            requisition.setStatus(newStatus);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid status: " + status + ". Allowed values: " +
                    Arrays.toString(RequisitionStatus.values()));
        }

        repository.save(requisition);
        log.info("Updated status of requisition {} to {}", id, status.toUpperCase());
    }

    public List<String> getAllTransactionNumber(Long companyId, Long branchId) {
        return repository.findByCompanyIdAndBranchIdAndStatusInAndApprovalStatus(
                        companyId,
                        branchId,
                        List.of(RequisitionStatus.PARTIALLY_ISSUED, RequisitionStatus.PENDING),
                        ApprovalStatus.APPROVED
                ).stream()
                .map(MaterialRequisitions::getTransactionNumber)
                .toList();
    }


//    public List<RequestedItemDTO> getItemsByTransactionNumber(String transactionNumber, Long companyId, Long branchId) {
//        MaterialRequisitions requisition = repository
//                .findByTransactionNumberAndCompanyIdAndBranchId(transactionNumber, companyId, branchId)
//                .orElseThrow(() -> new RuntimeException("Requisition not found"));
//
//        return requisition.getItems().stream().map(item -> {
//            RequestedItemDTO dto = new RequestedItemDTO();
//            dto.setName(item.getName());
//            dto.setCode(item.getCode());
//            dto.setType(item.getType());
//            dto.setQuantity(item.getQuantity());
//            return dto;
//        }).toList();
//    }

    public List<GroupedItemGroupDTO> getFullItemsByTransactionNumber(String transactionNumber, Long companyId, Long branchId) {
        MaterialRequisitions requisition = repository
                .findByTransactionNumberAndCompanyIdAndBranchId(transactionNumber, companyId, branchId)
                .orElseThrow(() -> new RuntimeException("Requisition not found"));

        List<GroupedItemGroupDTO> groupedItems = new ArrayList<>();

        // Handle individual items
        List<GroupedItemDTO> individualItems = new ArrayList<>();

        for (MaterialRequisitionItem item : requisition.getItems()) {
            // Removed: if (item.getIsIssued()) continue;

            int requestedQty = item.getQuantity();

            if ("item".equalsIgnoreCase(item.getType())) {
                Item itemMaster = itemRepository.findByCode(item.getCode()).orElse(null);

                individualItems.add(GroupedItemDTO.builder()
                        .code(item.getCode())
                        .name(itemMaster != null ? itemMaster.getName() : item.getName())
                        .uom(itemMaster != null ? itemMaster.getUom() : null)
                        .group(itemMaster != null ? itemMaster.getGroupName() : null)
                        .quantityRequested(requestedQty)
                        .stQuantity(itemMaster != null ? itemMaster.getStQty() : 0)
                        .build());
            }
        }

        if (!individualItems.isEmpty()) {
            groupedItems.add(GroupedItemGroupDTO.builder()
                    .type("item")
                    .parentBomCode(null)
                    .parentBomName(null)
                    .items(individualItems)
                    .build());
        }

        // Handle BOMs
        for (MaterialRequisitionItem item : requisition.getItems()) {
            // Removed: if (item.getIsIssued()) continue;

            if ("bom".equalsIgnoreCase(item.getType())) {
                int requestedQty = item.getQuantity();
                BOM bom = bomRepository.findByCode(item.getCode())
                        .orElseThrow(() -> new RuntimeException("BOM not found"));

                List<BomItem> bomItems = bomItemRepository.findByBom(bom);
                List<GroupedItemDTO> bomItemList = new ArrayList<>();

                for (BomItem bomItem : bomItems) {
                    Item bomItemMaster = itemRepository.findByCode(bomItem.getItemCode()).orElse(null);

                    Double bomItemQty = bomItem.getQuantity();
                    int qtyPerUnit = bomItemQty != null ? bomItemQty.intValue() : 0;
                    int totalQty = qtyPerUnit * requestedQty;

                    bomItemList.add(GroupedItemDTO.builder()
                            .code(bomItem.getItemCode())
                            .name(bomItemMaster != null ? bomItemMaster.getName() : bomItem.getItemName())
                            .uom(bomItemMaster != null ? bomItemMaster.getUom() : bomItem.getUom())
                            .group(bomItemMaster != null ? bomItemMaster.getGroupName() : null)
                            .quantityRequested(totalQty)
                            .stQuantity(bomItemMaster != null ? bomItemMaster.getStQty() : 0)
                            .build());
                }

                if (!bomItemList.isEmpty()) {
                    groupedItems.add(GroupedItemGroupDTO.builder()
                            .type("bom")
                            .parentBomCode(bom.getCode())
                            .parentBomName(bom.getName())
                            .items(bomItemList)
                            .build());
                }
            }
        }

        return groupedItems;
    }


}