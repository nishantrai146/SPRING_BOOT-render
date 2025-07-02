package com.lit.ims.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "vendor_customer_master")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    private String type;
    private String name;
    private String mobile;
    private String email;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String status;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;
}
