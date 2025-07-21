package com.lit.ims.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "batch_sequence_tracker")
public class BatchSequenceTracker {

    @Id
    private String batchPrefix; // e.g., MVN000185212350021072025

    private int lastSequence;

    private Long companyId;
    private Long branchId;
}
