package com.lit.ims.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeMasterDTO {
    private Long id;
    private String trno;
    private String name;
    private String status;
}
