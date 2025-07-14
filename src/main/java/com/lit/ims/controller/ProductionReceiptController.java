package com.lit.ims.controller;

import com.lit.ims.dto.ConfirmReceiptDTO;
import com.lit.ims.repository.ProductionReceiptRepository;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.ProductionReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}
