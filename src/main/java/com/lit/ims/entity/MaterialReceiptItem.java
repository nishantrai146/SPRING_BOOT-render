package com.lit.ims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "material_receipt_items",
        uniqueConstraints = @UniqueConstraint(columnNames = "batchNo")  // ✅ Uniqueness enforced here
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

    @Column(nullable = false)
    private String qc_status;

}
