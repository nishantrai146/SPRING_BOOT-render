package com.lit.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartMasterDT0 {
    private Long id;
    private String code;
    private String name;
    private String uom;
    private String status;
}
