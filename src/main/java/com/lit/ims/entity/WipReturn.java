package com.lit.ims.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lit.ims.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WipReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionNumber;

    private String returnType;

    private LocalDate returnDate;

    private Long workOrderId;

    private Long warehouseId;

    private Long companyId;

    private Long branchId;

    private String createdBy;

    private LocalDateTime createdAt;

    private String receiptNumber;

    @OneToMany(mappedBy = "wipReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WipReturnItem> returnItems;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ApprovalStatus approvalStatus;
}
