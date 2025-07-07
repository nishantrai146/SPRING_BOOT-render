package com.lit.ims.controller;

import com.lit.ims.dto.MaterialReceiptDTO;
import com.lit.ims.dto.MaterialReceiptItemDTO;
import com.lit.ims.dto.PendingQcItemsDTO;
import com.lit.ims.dto.UpdateQcStatusDTO;
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
                                                @RequestParam String quantity,
                                                @RequestAttribute("companyId") Long companyId,
                                                @RequestAttribute("branchId") Long branchId) {
        return ResponseEntity.ok(receiptService.generateBatchNumber(vendorCode, itemCode, quantity, companyId, branchId));
    }

    @GetMapping("/verify-batch")
    public ResponseEntity<ApiResponse<MaterialReceiptItemDTO>> verifyBatchNo(
            @RequestParam String batchNo,
            @RequestAttribute("companyId") Long companyId,
            @RequestAttribute("branchId") Long branchId) {
        return ResponseEntity.ok(receiptService.verifyBatchNumber(batchNo, companyId, branchId));
    }

    @GetMapping("/pending-qc")
    public ApiResponse<List<PendingQcItemsDTO>> getPendingQCItems(
            @RequestAttribute("companyId") Long companyId,
            @RequestAttribute("branchId") Long branchId
    ) {
        return receiptService.getPendingQcItems(companyId,branchId);
    }

    @GetMapping("/qc-status/result")
    public ResponseEntity<ApiResponse<List<PendingQcItemsDTO>>> getPassFailItems(
            @RequestAttribute("companyId")Long companyId,
            @RequestAttribute("branchId") Long branchId) {
        return ResponseEntity.ok(receiptService.getItemsWithPassOrFail(companyId, branchId));
    }

    @PutMapping("/qc-status/update")
    public ApiResponse<String > updateQcStatus(
            @RequestBody UpdateQcStatusDTO dto,
            @RequestAttribute("companyId") Long companyId,
            @RequestAttribute("branchId") Long branchId
    ){
        return receiptService.updateQcStatus(dto,companyId,branchId);
    }

}
