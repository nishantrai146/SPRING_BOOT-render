package com.lit.ims.controller;

import com.lit.ims.dto.MaterialReceiptDTO;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.MaterialReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/receipt")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MaterialReceiptController {

    private final MaterialReceiptService receiptService;

    // ✅ Save Receipt
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<MaterialReceiptDTO>> saveReceipt(@RequestBody MaterialReceiptDTO dto,
                                                           @RequestAttribute("companyId") Long companyId,
                                                           @RequestAttribute("branchId") Long branchId) {
        return ResponseEntity.ok(receiptService.saveReceipt(dto, companyId, branchId));
    }

    // ✅ Get All
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MaterialReceiptDTO>>> getAllReceipts(@RequestAttribute("companyId") Long companyId,
                                                                                @RequestAttribute("branchId") Long branchId) {
        return ResponseEntity.ok(receiptService.getAll(companyId, branchId));
    }

    // ✅ Generate Batch No
    @PostMapping("/generate-batch")
    public ResponseEntity<String> generateBatch(@RequestParam String vendorCode,
                                                @RequestParam String itemCode,
                                                @RequestAttribute("companyId") Long companyId,
                                                @RequestAttribute("branchId") Long branchId) {
        return ResponseEntity.ok(receiptService.generateBatchNumber(vendorCode, itemCode, companyId, branchId));
    }
}
