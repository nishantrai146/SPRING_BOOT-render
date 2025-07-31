package com.lit.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IssueQuantityDTO {
    private String itemCode;
    private Double totalIssuedQty;
}
