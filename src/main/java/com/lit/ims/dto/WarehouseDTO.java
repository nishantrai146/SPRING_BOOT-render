package com.lit.ims.dto;

import com.lit.ims.entity.WarehouseType;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseDTO {

    private Long id;

    @NotBlank(message = "Code is mandatory")
    private String code;

    private String trno;

    @NotBlank(message = "Name is mandatory")
    private String name;

    private String status;
    private WarehouseType type;
}
