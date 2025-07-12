package com.lit.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueProductionInfoDTO {
    private LocalDateTime issueDate;
    private String requisitionNumber;
    private LocalDateTime requisitionCreatedDate;
}
