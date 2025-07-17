package com.lit.ims.controller;

import com.lit.ims.dto.ProductionUsageDTO;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.ProductionUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}
