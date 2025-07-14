package com.lit.ims.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "production_receipt")
public class ProductionReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String transactionNumber;
    private String requisitionNumber;
    private String issueNumber;
    private LocalDate issueDate;
    private LocalDate receiptDate;
    private Long companyId;
    private Long branchId;
    private String createdBy;
    @OneToMany(mappedBy = "receipt",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<ProductionReceiptItem> items;
    private String type;

    @Enumerated(EnumType.STRING)
    private IssueStatus status;
}
