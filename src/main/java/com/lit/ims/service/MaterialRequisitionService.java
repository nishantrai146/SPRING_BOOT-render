package com.lit.ims.service;

import com.lit.ims.dto.MaterialRequisitionDTO;
import com.lit.ims.dto.RequestedItemDTO;
import com.lit.ims.dto.RequisitionSummaryDTO;
import com.lit.ims.entity.MaterialRequisitionItem;
import com.lit.ims.entity.MaterialRequisitions;
import com.lit.ims.entity.RequisitionStatus;
import com.lit.ims.repository.MaterialRequisitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialRequisitionService {

    private final MaterialRequisitionRepository repository;

    public MaterialRequisitions save(MaterialRequisitionDTO dto, Long companyId, Long branchId) {
        MaterialRequisitions requisitions = MaterialRequisitions.builder()
                .transactionNumber(dto.getTransactionNumber())
                .type(dto.getType())
                .companyId(companyId)
                .branchId(branchId)
                .status(RequisitionStatus.PENDING)
                .items(new ArrayList<>())
                .build();

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

        return repository.save(requisitions);
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
            requisition.setStatus(Enum.valueOf(RequisitionStatus.class, status.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid status: " + status);
        }

        repository.save(requisition);
    }

}
