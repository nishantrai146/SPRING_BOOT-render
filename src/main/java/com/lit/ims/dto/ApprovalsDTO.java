package com.lit.ims.dto;

import com.lit.ims.enums.ApprovalStatus;
import com.lit.ims.enums.ReferenceType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalsDTO {

    private Long id;
    private ReferenceType referenceType;
    private Long referenceId;
    private String requestedBy;
    private String requestedTo;
    private ApprovalStatus status;
    private String remarks;
    private LocalDateTime requestedDate;
    private LocalDateTime actionDate;
    private String metaData;
}
