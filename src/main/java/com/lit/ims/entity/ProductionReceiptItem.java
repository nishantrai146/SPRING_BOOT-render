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
@Table(name = "production_receipt_item")
public class ProductionReceiptItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemCode;
    private String itemName;
    private String batchNumber;
    private Double issuedQuantity;
    private Double receivedQuantity;
    private Double variance;
    private String note;
    @ManyToOne
    @JoinColumn(name = "receipt_id")
    private ProductionReceipt receipt;

}
