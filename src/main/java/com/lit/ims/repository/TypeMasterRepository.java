package com.lit.ims.repository;

import com.lit.ims.entity.TypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TypeMasterRepository extends JpaRepository<TypeMaster,Long> {
    boolean existsByName(String name);
    Optional<TypeMaster> findTopByOrderByIdDesc();
}
