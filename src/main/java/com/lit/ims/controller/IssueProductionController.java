package com.lit.ims.controller;

import com.lit.ims.dto.IssueProductionDTO;
import com.lit.ims.dto.IssuedItemSummaryResponseDTO;
import com.lit.ims.entity.IssueProduction;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.IssueProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issue-production")
@RequiredArgsConstructor
public class IssueProductionController {

    private final IssueProductionService issueProductionService;

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<IssueProduction>> saveIssue(
            @RequestBody IssueProductionDTO dto,
            @RequestAttribute Long companyId,
            @RequestAttribute Long branchId,
            @RequestAttribute String username
    ) {
        IssueProduction saved = issueProductionService.saveIssueProduction(dto, companyId, branchId, username);
        return ResponseEntity.ok(new ApiResponse<>(true, "Saved successfully", saved));
    }

    @GetMapping("/summary-by-issue")
    public ResponseEntity<ApiResponse<IssuedItemSummaryResponseDTO>> getSummaryByIssueNumber(
            @RequestParam String issueNumber,
            @RequestAttribute Long companyId,
            @RequestAttribute Long branchId
    ) {
        IssuedItemSummaryResponseDTO summary = issueProductionService
                .getGroupedIssuedItemsWithMeta(issueNumber, companyId, branchId);

        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched summary", summary));
    }



    @GetMapping("/all-issue-numbers")
    public ResponseEntity<ApiResponse<List<String>>> getAllIssueNumbers(
            @RequestAttribute Long companyId,
            @RequestAttribute Long branchId
    ) {
        List<String> issueNumbers = issueProductionService.getAllIssueNumbers(companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched issue numbers", issueNumbers));
    }






}
