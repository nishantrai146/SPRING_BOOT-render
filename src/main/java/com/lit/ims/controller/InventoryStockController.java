package com.lit.ims.controller;

import com.lit.ims.dto.InventoryStockDTO;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.InventoryStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryStockController {

    private final InventoryStockService inventoryStockService;

    @GetMapping("/warehouse/{warehouseId}")
    public ApiResponse<List<InventoryStockDTO>> getItemsByWarehouse(
            @PathVariable Long warehouseId,
            @RequestAttribute("companyId") Long companyId,
            @RequestAttribute("branchId") Long branchId) {

        List<InventoryStockDTO> items = inventoryStockService.getItemsByWarehouse(warehouseId, companyId, branchId);
        return new ApiResponse<>(true, "Items fetched successfully", items);
    }
}
