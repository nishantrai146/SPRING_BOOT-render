package com.lit.ims.repository;

import com.lit.ims.entity.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog,Long> {
    List<TransactionLog> findAllByOrderByTimestampDesc();

    List<TransactionLog> findTop10ByOrderByTimestampDesc();
}
