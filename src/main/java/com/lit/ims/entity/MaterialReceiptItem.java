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

    private String name;
    private String code;
    private Integer quantity;
    @Column(nullable = false)
    private String batchNo;

    @ManyToOne
    @JoinColumn
    private MaterialReceipt receipt;

}
