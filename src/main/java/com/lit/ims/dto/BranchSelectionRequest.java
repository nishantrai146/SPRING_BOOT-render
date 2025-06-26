package com.lit.ims.dto;

import lombok.Data;

@Data
public class BranchSelectionRequest {
    private String username;
    private Long branchId;
}
