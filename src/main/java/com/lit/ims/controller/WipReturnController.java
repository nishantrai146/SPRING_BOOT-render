package com.lit.ims.controller;

import com.lit.ims.dto.WipReturnDTO;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.WipReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wip-return")
@RequiredArgsConstructor
public class WipReturnController {

    private final WipReturnService wipReturnService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> createWipReturn(
            @RequestBody WipReturnDTO dto,
            @RequestAttribute Long companyId,
            @RequestAttribute Long branchId,
            @RequestAttribute String username) {

        wipReturnService.saveWipReturn(dto, companyId, branchId, username);
        return ResponseEntity.ok(new ApiResponse<>(true,"WIP Return saved successfully",null));
    }
    @GetMapping("/recent/summary")
    public ApiResponse<List<Map<String, Object>>> getRecentWipReturnSummary(
            @RequestAttribute("companyId") Long companyId,
            @RequestAttribute("branchId") Long branchId
    ) {
        List<Map<String, Object>> summaryList = wipReturnService.getRecentWipReturnSummary(companyId, branchId);
        return new ApiResponse<>(true, "Recent WIP return summary", summaryList);
    }

    @GetMapping("/count-defective")
    public ResponseEntity<ApiResponse<Long>> countDefectiveReturns(
            @RequestAttribute Long companyId,
            @RequestAttribute Long branchId
    ) {
        long count = wipReturnService.countDefectiveReturns(companyId, branchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Defective WIP Returns count fetched", count));
    }



}
