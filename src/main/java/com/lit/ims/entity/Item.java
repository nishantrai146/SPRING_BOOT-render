package com.lit.ims.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "item_master")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    private String uom;
    private String type;
    private String barcode;

    @Column(name = "`group`")
    private String groupName;

    private String status;
    private Double price;
    private Integer stQty;
    private Integer life;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    private boolean isInventoryItem;
    private boolean isIqc;
}
