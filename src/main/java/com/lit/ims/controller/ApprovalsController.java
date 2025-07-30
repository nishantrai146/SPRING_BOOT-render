package com.lit.ims.controller;


import com.lit.ims.dto.ApprovalsDTO;
import com.lit.ims.dto.StockAdjustmentApprovalDTO;
import com.lit.ims.entity.Approvals;
import com.lit.ims.entity.Role;
import com.lit.ims.entity.User;
import com.lit.ims.enums.ApprovalStatus;
import com.lit.ims.repository.ApprovalsRepository;
import com.lit.ims.repository.UserRepository;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.ApprovalsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalsController {

    private final ApprovalsService service;

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ApprovalsDTO>>> getMyApprovals(
            @RequestAttribute String username,
            @RequestAttribute Long companyId,
            @RequestAttribute Long branchId
    ) {
        List<ApprovalsDTO> approvals = service.getMyApprovals(username, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched approvals", approvals));
    }


    @PostMapping("/{id}/action")
    public ResponseEntity<ApiResponse<String>> takeAction(
            @PathVariable Long id,
            @RequestParam ApprovalStatus status,
            @RequestParam(required = false) String remarks,
            @RequestAttribute String username
    ) {
        service.takeAction(id, status, remarks,username);
        return ResponseEntity.ok(new ApiResponse<>(true, "Action taken successfully", null));
    }

    @PostMapping("/stock-adjustment")
    public ResponseEntity<ApiResponse<String>> requestStockAdjustmentApproval(
            @RequestBody StockAdjustmentApprovalDTO dto,
            @RequestAttribute Long companyId,
            @RequestAttribute Long branchId,
            @RequestAttribute String username
    ) {
        service.createStockAdjustmentApproval(dto, username, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Stock adjustment approval request submitted successfully",null));
    }
}
