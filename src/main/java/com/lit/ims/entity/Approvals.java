package com.lit.ims.entity;

import com.lit.ims.enums.ApprovalStage;
import com.lit.ims.enums.ApprovalStatus;
import com.lit.ims.enums.ReferenceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Approvals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ReferenceType referenceType;

    private Long referenceId;

    private String requestedBy;
    private String requestedTo;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus status;

    private String remarks;

    private Long companyId;
    private Long branchId;

    private LocalDateTime requestedDate;
    private LocalDateTime actionDate;

    @Column(columnDefinition = "TEXT")
    private String metaData;

    @Enumerated(EnumType.STRING)
    private ApprovalStage stage;

}
