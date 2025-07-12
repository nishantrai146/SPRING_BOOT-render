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
                .findByCompanyIdAndBranchId(companyId, branchId)
                .stream()
                .map(IssueProduction::getIssueNumber)
                .distinct()
                .toList();
    }

    public IssuedItemSummaryResponseDTO getGroupedIssuedItemsWithMeta(String issueNumber, Long companyId, Long branchId) {
        Optional<IssueProduction> optionalIssue = issueProductionRepository
                .findByIssueNumberAndCompanyIdAndBranchId(issueNumber, companyId, branchId);

        if (optionalIssue.isEmpty()) {
            return null;
        }

        IssueProduction issue = optionalIssue.get();
        String reqNumber = issue.getRequisitionNumber();
        LocalDateTime issueDate = issue.getIssueDate();
        LocalDateTime requisitionCreatedAt = materialRequisitionRepository
                .findByTransactionNumberAndCompanyIdAndBranchId(reqNumber, companyId, branchId)
                .map(MaterialRequisitions::getCreatedAt)
                .orElse(null);

        List<IssuedItemSummaryDTO> groupedList = new ArrayList<>();
        Map<String, IssuedItemSummaryDTO> groupedMap = new HashMap<>();

        for (IssuedBatchItems batch : issue.getBatchItems()) {
            String key = batch.getItemCode();

            groupedMap.compute(key, (k, existing) -> {
                if (existing == null) {
                    return new IssuedItemSummaryDTO(
                            batch.getItemCode(),
                            batch.getItemName(),
                            batch.getQuantity(),
                            batch.getVariance(),
                            new ArrayList<>(List.of(batch.getBatchNo()))
                    );
                } else {
                    existing.setTotalIssued(existing.getTotalIssued() + batch.getQuantity());
                    existing.setTotalVariance(existing.getTotalVariance() + batch.getVariance());
                    existing.getBatchNumbers().add(batch.getBatchNo());
                    return existing;
                }
            });
        }

        groupedList.addAll(groupedMap.values());

        return IssuedItemSummaryResponseDTO.builder()
                .issueNumber(issueNumber)
                .requisitionNumber(reqNumber)
                .requisitionCreatedAt(requisitionCreatedAt)
                .issueDate(issueDate)
                .type(issue.getType())
                .items(groupedList)
                .build();
    }




}
