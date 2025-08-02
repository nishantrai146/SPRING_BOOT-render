    package com.lit.ims.controller;

    import com.lit.ims.dto.*;
    import com.lit.ims.response.ApiResponse;
    import com.lit.ims.service.FileStorageService;
    import com.lit.ims.service.MaterialReceiptService;
    import org.springframework.core.io.Resource;
    import lombok.RequiredArgsConstructor;
    import org.springframework.format.annotation.DateTimeFormat;
    import org.springframework.http.HttpHeaders;
    import org.springframework.http.MediaType;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;


    import java.time.LocalDate;
    import java.util.List;
    import java.util.Map;

    @RestController
    @RequestMapping("/api/receipt")
    @RequiredArgsConstructor
    @CrossOrigin(origins = "*")
    public class MaterialReceiptController {

        private final MaterialReceiptService receiptService;
        private final FileStorageService fileStorageService;

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
            return ResponseEntity.ok(receiptService.verifyBatchAndFetchDetails(batchNo, companyId, branchId));
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

        @PutMapping(value = "/qc-status/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ApiResponse<String> updateQcStatus(
                @ModelAttribute UpdateQcStatusDTO dto, // <-- FIXED
                @RequestAttribute("companyId") Long companyId,
                @RequestAttribute("branchId") Long branchId,
                @RequestAttribute("username") String username
        ) {
            receiptService.updateQcStatus(dto, companyId, branchId, username);
            return new ApiResponse<>(true, "QC status updated successfully", null);
        }



        @GetMapping("/qc/item-by-batch")
        public ResponseEntity<ApiResponse<PendingQcItemsDTO>> getItemByBatchNo(
                @RequestParam String batchNo,
                @RequestAttribute Long companyId,
                @RequestAttribute Long branchId
        ) {
            ApiResponse<PendingQcItemsDTO> response = receiptService.getItemByBatchNo(batchNo, companyId, branchId);
            return ResponseEntity.ok(response);
        }
        @DeleteMapping("/delete-pending/{id}")
        public ResponseEntity<ApiResponse<String>> deletePendingTransaction(
                @PathVariable Long id,
                @RequestAttribute Long companyId,
                @RequestAttribute Long branchId
        ){
            ApiResponse<String> response=receiptService.deletePendingQc(id,companyId,branchId);
            return ResponseEntity.ok(response);
        }

        @PostMapping("/batches/reserve")
        public ResponseEntity<ApiResponse<MaterialReceiptItemDTO>> reserveBatch(
                @RequestParam String batchNo,
                @RequestAttribute("companyId") Long companyId,
                @RequestAttribute("branchId") Long branchId,
                @RequestAttribute("username") String username) {
            return ResponseEntity.ok(receiptService.verifyBatchAndReserveIfFifo(batchNo, companyId, branchId, username));
        }

        @DeleteMapping("/release-reservation")
        public ResponseEntity<ApiResponse<String>> releaseReservedBatch(
                @RequestParam String batchNo,
                @RequestAttribute("companyId") Long companyId,
                @RequestAttribute("branchId") Long branchId,
                @RequestAttribute("username") String username
        ) {
            return ResponseEntity.ok(
                    receiptService.releaseReservedBatch(batchNo, companyId, branchId, username)
            );
        }


        @PostMapping("/batches/confirm")
        public ResponseEntity<ApiResponse<String>> confirmIssuedBatch(
                @RequestParam String batchNo,
                @RequestAttribute("companyId") Long companyId,
                @RequestAttribute("branchId") Long branchId,
                @RequestAttribute("username") String username) {

            return ResponseEntity.ok(
                    receiptService.confirmIssuedBatch(batchNo, companyId, branchId, username));
        }

        @GetMapping("/iqc-count")
        public ApiResponse<IqcStatusCountDTO> getIqcStatusCounts(
                @RequestAttribute("companyId") Long companyId,
                @RequestAttribute("branchId") Long branchId) {
            return receiptService.getIqcStatusCounts(companyId, branchId);
        }

        @GetMapping("/items-by-date")
        public ResponseEntity<ApiResponse<Map<String, Object>>> getItemsByDate(
                @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                @RequestAttribute Long companyId,
                @RequestAttribute Long branchId
        ) {
            Map<String, Object> result = receiptService.getItemsByDate(date, companyId, branchId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Items fetched successfully", result));
        }

        @GetMapping("/qc-status/attachment/{fileName:.+}")
        public ResponseEntity<Resource> downloadAttachment(@PathVariable String fileName) {
            Resource resource = fileStorageService.loadFile( fileName);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        }






    }
