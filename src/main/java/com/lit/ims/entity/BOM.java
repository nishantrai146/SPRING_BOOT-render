package com.lit.ims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BOM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String code;
    private String status;

    @OneToMany(mappedBy = "bom",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<BomItem> items;
    @Column(nullable = false)
    private Long companyId;
    @Column(nullable = false)
    private Long branchId;
}
