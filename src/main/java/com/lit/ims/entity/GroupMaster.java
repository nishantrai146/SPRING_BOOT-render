package com.lit.ims.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "group_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String trno;

    @Column(nullable = false, unique = true)
    private String name;

    private String status;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private Long branchId;
}
