package com.lit.ims.service;

import com.lit.ims.dto.*;
import com.lit.ims.entity.*;
import com.lit.ims.repository.IssueProductionRepository;
import com.lit.ims.repository.MaterialRequisitionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
public class IssueProductionService {

    private final IssueProductionRepository issueProductionRepository;
    private final MaterialRequisitionRepository materialRequisitionRepository;

    public IssueProduction saveIssueProduction(IssueProductionDTO dto, Long companyId, Long branchId, String username) {

        MaterialRequisitions requisition = materialRequisitionRepository
                .findByTransactionNumberAndCompanyIdAndBranchId(
                        dto.getRequisitionNumber(), companyId, branchId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Requisition " + dto.getRequisitionNumber() + " not found"));


        IssueProduction issue = IssueProduction.builder()
                .issueNumber(dto.getIssueNumber())
                .requisitionNumber(dto.getRequisitionNumber())
                .companyId(companyId)
                .branchId(branchId)
                .createdBy(username)
                .issueDate(LocalDateTime.now())
                .status(IssueStatus.PENDING)
                .type(requisition.getType())
                .build();

        List<IssuedBatchItems> batchItems = dto.getBatchItems().stream()
                .map(batch -> toEntity(batch, issue))
                .toList();

        issue.setBatchItems(batchItems);
        IssueProduction savedIssue = issueProductionRepository.save(issue);

        requisition.setStatus(RequisitionStatus.APPROVED);
        materialRequisitionRepository.save(requisition);


        return savedIssue;
    }

    private IssuedBatchItems toEntity(IssuedBatchItemDTO dto, IssueProduction issue) {
        return IssuedBatchItems.builder()
                .itemCode(dto.getItemCode())
                .itemName(dto.getItemName())
                .batchNo(dto.getBatchNo())
                .quantity(dto.getQuantity())
                .issuedQty(dto.getIssuedQty())
                .variance(dto.getVariance())
                .issue(issue)
                .build();
    }


    public List<String> getAllIssueNumbers(Long companyId, Long branchId) {
        return issueProductionRepository
                .findAllByStatusAndCompanyIdAndBranchId(IssueStatus.PENDING,companyId,branchId)
                .stream()
                .map(IssueProduction::getIssueNumber)
                .distinct()
                .toList();
    }

    public IssuedItemSummaryResponseDTO getIssuedBatchesWithMeta(
            String issueNumber, Long companyId, Long branchId) {

        IssueProduction issue = issueProductionRepository
                .findByIssueNumberAndCompanyIdAndBranchId(issueNumber, companyId, branchId)
                .orElse(null);

        if (issue == null) return null;

        String reqNumber = issue.getRequisitionNumber();
        LocalDateTime issueDate = issue.getIssueDate();
        LocalDateTime requisitionCreatedAt = materialRequisitionRepository
                .findByTransactionNumberAndCompanyIdAndBranchId(reqNumber, companyId, branchId)
                .map(MaterialRequisitions::getCreatedAt)
                .orElse(null);

        /* 🔄 Build a flat list: one DTO per batch row */
        List<IssuedItemSummaryDTO> items = issue.getBatchItems().stream()
                .map(b -> IssuedItemSummaryDTO.builder()
                        .id(b.getId())
                        .itemCode(b.getItemCode())
                        .itemName(b.getItemName())
                        .totalIssued(b.getQuantity())
                        .totalVariance(b.getVariance())
                        .batchNumber(b.getBatchNo())
                        .build())
                .toList();

        return IssuedItemSummaryResponseDTO.builder()
                .issueNumber(issueNumber)
                .requisitionNumber(reqNumber)
                .requisitionCreatedAt(requisitionCreatedAt)
                .issueDate(issueDate)
                .type(issue.getType())
                .items(items)                   // 🆕 flat list
                .build();
    }




}
