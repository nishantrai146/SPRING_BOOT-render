package com.lit.ims.controller;

import com.lit.ims.dto.*;
import com.lit.ims.entity.MaterialRequisitions;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.MaterialRequisitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requisitions")
@RequiredArgsConstructor
public class MaterialRequisitionController {

    private final MaterialRequisitionService service;

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<String>> createRequisition(
            @RequestBody MaterialRequisitionDTO dto,
            @RequestAttribute Long companyId,
            @RequestAttribute Long branchId,
            @RequestAttribute String username
    ) {
        MaterialRequisitions saved = service.save(dto, companyId, branchId,username);
        return ResponseEntity.ok(new ApiResponse<>(true,"Requisition saved successfully", saved.getTransactionNumber()));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<RequisitionSummaryDTO>>> getRecent(
            @RequestAttribute Long companyId,
            @RequestAttribute Long branchId
    ) {
        List<RequisitionSummaryDTO> list = service.getRecent(companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true,"Fetch Successfully",list));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateRequisitionStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestAttribute Long companyId,
            @RequestAttribute Long branchId
    ) {
        service.updateStatus(id, status, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true,"Status updated successfully", status.toUpperCase()));
    }
    @GetMapping("/trNo")
    public ResponseEntity<ApiResponse <List<String>>>getAllTransactionNumber(@RequestAttribute Long companyId,
                                                                       @RequestAttribute Long branchId){
        List<String> transactions=service.getAllTransactionNumber(companyId,branchId);
        return ResponseEntity.ok(new ApiResponse<>(true,"Fetch Successfully",transactions));
    }

    @GetMapping("/{transactionNumber}/items/full")
    public ResponseEntity<ApiResponse<List<GroupedItemGroupDTO>>> getFullItemDetailsByTransactionNumber(
            @PathVariable String transactionNumber,
            @RequestAttribute Long companyId,
            @RequestAttribute Long branchId
    ) {
        List<GroupedItemGroupDTO> items = service.getFullItemsByTransactionNumber(transactionNumber, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched full item details", items));
    }



}
