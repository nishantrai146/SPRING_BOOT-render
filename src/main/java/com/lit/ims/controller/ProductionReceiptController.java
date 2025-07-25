package com.lit.ims.controller;

import com.lit.ims.dto.ConfirmReceiptDTO;
import com.lit.ims.dto.ProductionReceiptItemDTO;
import com.lit.ims.dto.ProductionReceiptTableDTO;
import com.lit.ims.dto.ReceiptIdNumberDTO;
import com.lit.ims.repository.ProductionReceiptRepository;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.ProductionReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/production-receipt")
@RequiredArgsConstructor
public class ProductionReceiptController {
    private final ProductionReceiptService productionReceiptService;
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirm(
            @RequestBody ConfirmReceiptDTO dto,
            @RequestAttribute Long companyId,
            @RequestAttribute Long branchId,
            @RequestAttribute String username
            ){
        productionReceiptService.confirmReceipt(dto,companyId,branchId,username);
        return ResponseEntity.ok(new ApiResponse<>(true,"Receipt Confirmed",null));
    }

    @GetMapping("/table")
    public ResponseEntity<ApiResponse<List<ProductionReceiptTableDTO>>> listTable(
            @RequestAttribute Long companyId,
            @RequestAttribute Long branchId) {

        List<ProductionReceiptTableDTO> data =
                productionReceiptService.listReceiptsForTable(companyId, branchId);

        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched receipts", data));
    }

    @GetMapping("/receipts")
    public ResponseEntity<ApiResponse<List<ReceiptIdNumberDTO>>> getReceiptIdAndNumbers(
            @RequestAttribute Long companyId,
            @RequestAttribute Long branchId) {

        List<ReceiptIdNumberDTO> result = productionReceiptService.getAllReceiptIdAndNumbers(companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true,"Receipt Number", result));
    }
    @GetMapping("/receipts/{id}/items")
    public ResponseEntity<ApiResponse<List<ProductionReceiptItemDTO>>> getReceiptItems(
            @PathVariable Long id,
            @RequestAttribute Long companyId,
            @RequestAttribute Long branchId) {

        List<ProductionReceiptItemDTO> items = productionReceiptService.getReceiptItemsById(id, companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true,"Fetched Data", items));
    }




}
