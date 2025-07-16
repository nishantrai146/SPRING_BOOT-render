package com.lit.ims.controller;

import com.lit.ims.dto.*;
import com.lit.ims.entity.AdjustmentStatus;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.StockAdjustmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/stock-adjustments/requests")
@RequiredArgsConstructor
public class StockAdjustmentRequestController {

    private final StockAdjustmentService stockAdjustmentService;

    // 🟢 1. Operator creates a quantity adjustment request
    @PostMapping
    public ApiResponse<AdjustmentRequestResponseDTO> requestChange(
            @Valid @RequestBody CreateAdjustmentRequestDTO dto,
            @RequestAttribute Long companyId,
            @RequestAttribute Long branchId,
            Principal principal) {

        return stockAdjustmentService.createRequest(
                dto, companyId, branchId, principal.getName());
    }

    // 🔵 2. Admin views all requests (filtered by status: PENDING, APPROVED, REJECTED)
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    public List<AdjustmentRequestResponseDTO> listRequests(
            @RequestParam(defaultValue = "PENDING") AdjustmentStatus status,
            @RequestAttribute Long companyId,
            @RequestAttribute Long branchId) {

        return stockAdjustmentService.findByStatus(status, companyId, branchId);
    }

    // ✅ 3. Admin approves a request
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    public ApiResponse<Void> approve(
            @PathVariable Long id,
            Principal principal) {

        return stockAdjustmentService.approve(id, principal.getName());
    }

    // ❌ 4. Admin rejects a request (reason is required)
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    public ApiResponse<Void> reject(
            @PathVariable Long id,
            @RequestParam String reason,
            Principal principal) {

        return stockAdjustmentService.reject(id, principal.getName(), reason);
    }
}
