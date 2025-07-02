package com.lit.ims.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vendor_items_master", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"company_id", "branch_id", "vendor_code", "item_code"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorItemsMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "vendor_code")
    private String vendorCode;

    private String vendorName;

    @Column(name = "item_code")
    private String itemCode;

    private String itemName;

    private Integer days;
    private Integer quantity;
    private Double price;
    private String status;
}
