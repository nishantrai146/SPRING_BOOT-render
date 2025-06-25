package com.lit.ims.repository;

import com.lit.ims.entity.LogInLog;
import com.lit.ims.entity.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogInRepository  extends JpaRepository<LogInLog,Long> {
    List<LogInLog> findAllByOrderByTimestampDesc();

}
