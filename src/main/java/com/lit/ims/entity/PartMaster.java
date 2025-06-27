package com.lit.ims.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "part_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String uom;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private Long branchId;
}
