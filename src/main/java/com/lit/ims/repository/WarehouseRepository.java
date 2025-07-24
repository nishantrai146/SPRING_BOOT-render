package com.lit.ims.repository;

import com.lit.ims.entity.Warehouse;
import com.lit.ims.entity.WarehouseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findTopByCompanyIdAndBranchIdOrderByIdDesc(Long companyId, Long branchId);

    boolean existsByCodeAndCompanyIdAndBranchId(String code, Long companyId, Long branchId);

    List<Warehouse> findAllByCompanyIdAndBranchId(Long companyId, Long branchId);

    Optional<Warehouse> findByIdAndCompanyIdAndBranchId(Long id, Long companyId, Long branchId);

    List<Warehouse> findByTypeInAndCompanyIdAndBranchId(List<WarehouseType> types,Long companyId,Long branchID);
    Optional<Warehouse> findByTypeAndCompanyIdAndBranchId(WarehouseType type, Long companyId, Long branchId);
}
