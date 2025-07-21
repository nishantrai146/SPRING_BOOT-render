package com.lit.ims.repository;

import com.lit.ims.entity.BatchSequenceTracker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BatchSequenceTrackerRepository extends JpaRepository<BatchSequenceTracker, String> {
    Optional<BatchSequenceTracker> findByBatchPrefixAndCompanyIdAndBranchId(String batchPrefix, Long companyId, Long branchId);
}

