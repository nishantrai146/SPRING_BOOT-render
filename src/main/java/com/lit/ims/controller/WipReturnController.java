package com.lit.ims.controller;

import com.lit.ims.dto.WipReturnDTO;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.WipReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
