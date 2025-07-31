package com.lit.ims.controller;

import com.lit.ims.dto.WarehouseTransferLogDTO;
import com.lit.ims.dto.WarehouseTransferLogFilterDTO;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.WarehouseTransferLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouse-transfer-logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WarehouseTransferLogController {

    private final WarehouseTransferLogService transferLogService;

    @PostMapping(value = "/filter", produces = "application/json")
    public ApiResponse<List<WarehouseTransferLogDTO>> getTransferLogs(
            @RequestAttribute("companyId") Long companyId,
            @RequestAttribute("branchId") Long branchId,
            @RequestBody @Valid WarehouseTransferLogFilterDTO filter
    ) {
        List<WarehouseTransferLogDTO> logs = transferLogService.getTransferLogsByItemAndSourceWarehouse(
                companyId, branchId, filter.getItemCode(), filter.getSourceWarehouseId()
        );
        return new ApiResponse<>(true, "Fetched transactions", logs);
    }
}
