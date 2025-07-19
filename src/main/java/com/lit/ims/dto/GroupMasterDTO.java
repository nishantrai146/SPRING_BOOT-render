package com.lit.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMasterDTO {
    private Long id;
    private String trno;
    private String name;
    private String status;
    private String groupCode;
}
