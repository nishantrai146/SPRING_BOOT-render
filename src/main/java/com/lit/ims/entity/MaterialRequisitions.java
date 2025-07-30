package com.lit.ims.entity;

import com.lit.ims.entity.RequisitionStatus;
import com.lit.ims.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "material_requisitions")
public class MaterialRequisitions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionNumber;
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private RequisitionStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

//    @PrePersist
//    protected void onCreate() {
//        this.createdAt = LocalDateTime.now();
//        if (this.status == null) {
//            this.status = RequisitionStatus.PENDING;
//        }
//    }

    private Long companyId;
    private Long branchId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ApprovalStatus approvalStatus;
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = RequisitionStatus.PENDING;
        }
        if (this.approvalStatus == null) {
            this.approvalStatus = ApprovalStatus.PENDING;
        }
    }

    @OneToMany(mappedBy = "requisition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaterialRequisitionItem> items;
}
