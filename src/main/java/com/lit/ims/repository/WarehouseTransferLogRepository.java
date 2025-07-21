package com.lit.ims.repository;

import com.lit.ims.entity.WarehouseTransferLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseTransferLogRepository extends JpaRepository<WarehouseTransferLog, Long> {
}
