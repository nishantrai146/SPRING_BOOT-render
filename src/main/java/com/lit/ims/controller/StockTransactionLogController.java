package com.lit.ims.controller;

import com.lit.ims.dto.StockTransactionLogDTO;
import com.lit.ims.dto.StockTransactionLogFilterDTO;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.StockTransactionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock-logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockTransactionLogController {

    private final StockTransactionLogService logService;

    @PostMapping("/filter")
    public ApiResponse<List<StockTransactionLogDTO>> getLogs(
            @RequestAttribute Long companyId,
            @RequestAttribute Long branchId,
            @RequestBody StockTransactionLogFilterDTO filterDTO
    ) {
        List<StockTransactionLogDTO> result = logService.getLogsWithFilters(companyId, branchId, filterDTO);
        return new ApiResponse<>(true, "Fetch Successful", result);
    }

}
