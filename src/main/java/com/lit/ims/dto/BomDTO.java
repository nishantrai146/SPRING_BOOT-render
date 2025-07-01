package com.lit.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BomDTO {
    private Long id;
    private String name;
    private String code;
    private String status;
    private List<BomItemDTO> items;

}
