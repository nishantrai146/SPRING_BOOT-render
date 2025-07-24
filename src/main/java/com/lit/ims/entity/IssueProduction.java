package com.lit.ims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "issue_production_master")
public class IssueProduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String issueNumber;

    private String requisitionNumber;

    private Long companyId;
    private Long branchId;

    private LocalDateTime issueDate;

    @Column(name = "issued_by")
    private String createdBy;

    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IssuedBatchItems> batchItems;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus status;

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = IssueStatus.PENDING;
        }
    }

    @Column
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;


}
