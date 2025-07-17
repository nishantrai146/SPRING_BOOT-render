package com.lit.ims.controller;

import com.lit.ims.dto.ProductionUsageDTO;
import com.lit.ims.dto.ProductionUsageResponseDTO;
import com.lit.ims.dto.ProductionUsageSummaryDTO;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.ProductionUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/production-usage")
@RequiredArgsConstructor
public class ProductionUsageController {

    private final ProductionUsageService productionUsageService;

    @PostMapping("/save")
    public ApiResponse<String> saveProductionUsage(
            @RequestBody ProductionUsageDTO dto,
            @RequestAttribute Long companyId,
            @RequestAttribute Long branchId
            ){
        return productionUsageService.saveProductionUsage(dto,companyId,branchId);
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<ProductionUsageSummaryDTO>>> getUsageSummaries(
            @RequestAttribute Long companyId,
            @RequestAttribute Long branchId
    ) {
        List<ProductionUsageSummaryDTO> summaries = productionUsageService.getAllUsageSummaries(companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched successfully", summaries));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductionUsageResponseDTO>> getUsageDetailsById(
            @PathVariable Long id,
            @RequestAttribute Long companyId,
            @RequestAttribute Long branchId) {

        ProductionUsageResponseDTO dto = productionUsageService.getUsageDetailsById(id, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched successfully", dto));
    }

}
