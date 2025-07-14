package com.lit.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IqcStatusCountDTO {
    private long pendingCount;
    private long passCount;
    private long failCount;
}
