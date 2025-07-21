package com.lit.ims.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "stock_transaction_log")
public class StockTransactionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemCode;
    private String itemName;
    private Long companyId;
    private Long branchId;

    private String transactionType; // INCREASE / DECREASE
    private int quantityChanged;

    private String referenceType; // e.g., "MaterialReceipt", "IssueToProduction"
    private Long referenceId;

    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    private LocalDateTime transactionDate;

    private String remarks;
}
