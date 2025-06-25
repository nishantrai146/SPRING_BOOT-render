package com.lit.ims.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "vendor_customer_master")
@NoArgsConstructor
@AllArgsConstructor
public class VendorCustomer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private String name;
    private String mobile;
    private String email;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String status;
}
