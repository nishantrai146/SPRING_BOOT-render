package com.lit.ims.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "issued_batch_items")
@Builder
public class IssuedBatchItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;
    private String itemCode;
    private String batchNo;
    private Double quantity;
    private Double issuedQty;
    private Double variance;

    @ManyToOne
    @JoinColumn(name = "issue_id")
    @JsonIgnore
    private IssueProduction issue;
}
