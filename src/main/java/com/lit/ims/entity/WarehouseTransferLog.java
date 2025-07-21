package com.lit.ims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_transfer_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseTransferLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemCode;
    private String itemName;
    private Integer quantity;

    private Long sourceWarehouseId;
    private String sourceWarehouseName;

    private Long targetWarehouseId;
    private String targetWarehouseName;

    private String transferType;
    private String referenceType;
    private Long referenceId;

    private Long companyId;
    private Long branchId;

    private String transferredBy;
    private LocalDateTime transferredAt;
}
