package com.lit.ims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "material_receipt_items",
        uniqueConstraints = @UniqueConstraint(columnNames = "batchNo")
)
public class MaterialReceiptItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String itemName;
    @Column(nullable = false)
    private String itemCode;
    private Integer quantity;
    @Column(nullable = false)
    private String batchNo;

    @ManyToOne
    @JoinColumn
    private MaterialReceipt receipt;

    @Column(nullable = false,name = "qc_status")
    private String qcStatus;

    @Column(nullable = false, updatable = false)

    private LocalDateTime createdAt;


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
