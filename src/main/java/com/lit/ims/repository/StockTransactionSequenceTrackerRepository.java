package com.lit.ims.repository;

import com.lit.ims.entity.StockTransactionSequenceTracker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface StockTransactionSequenceTrackerRepository extends JpaRepository<StockTransactionSequenceTracker, Long> {
    Optional<StockTransactionSequenceTracker> findByDate(LocalDate date);
}
