package com.lit.ims.repository;

import com.lit.ims.entity.PartMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartMasterRepository extends JpaRepository<PartMaster,Long> {
    Optional<PartMaster> findByCode(String code);
    boolean existsByCode(String code);
}
