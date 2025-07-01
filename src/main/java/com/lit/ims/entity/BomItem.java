package com.lit.ims.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BomItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "item_code")
    private String itemCode;

    @Column(name = "uom")
    private String uom;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "warehouse_name")
    private String warehouseName;

    @ManyToOne
    @JoinColumn(name = "bom_id")
    @JsonIgnore
    private BOM bom;
}
