package com.lit.ims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "material_receipt")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mode;
    private String vendor;
    private String vendorCode;
    private Long companyId;
    private Long branchId;

    @OneToMany(mappedBy = "receipt",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<MaterialReceiptItem> items=new ArrayList<>();

    @Column(nullable = false, updatable = false,name = "entry_date")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
