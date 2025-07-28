package com.lit.ims.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WipReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long itemCode;

    private String itemName;

    @Column(name = "original_batch_no")
    private String originalBatchNo;

    @Column(name = "new_batch_no")
    private String newBatchNo;

    private Integer originalQty;

    private Integer returnQty;

    private String returnReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wip_return_id")
    @JsonIgnore
    private WipReturn wipReturn;
}
