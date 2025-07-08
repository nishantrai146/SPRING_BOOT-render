package com.lit.ims.controller;

import com.lit.ims.dto.MaterialRequisitionDTO;
import com.lit.ims.dto.RequisitionSummaryDTO;
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
            @RequestAttribute Long branchId
    ) {
        MaterialRequisitions saved = service.save(dto, companyId, branchId);
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

}
