package com.lit.ims.repository;

import com.lit.ims.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse,Long> {
    Optional<Warehouse> findTopByOrderByIdDesc();
    boolean existsByTrno(String trno);
    boolean existsByCode(String code);
}
