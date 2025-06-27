package com.lit.ims.repository;

import com.lit.ims.entity.PartMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartMasterRepository extends JpaRepository<PartMaster, Long> {

    Optional<PartMaster> findByIdAndCompanyIdAndBranchId(Long id, Long companyId, Long branchId);

    List<PartMaster> findByCompanyIdAndBranchId(Long companyId, Long branchId);

    boolean existsByCodeAndCompanyIdAndBranchId(String code, Long companyId, Long branchId);

    boolean existsByNameAndCompanyIdAndBranchId(String name, Long companyId, Long branchId);
}
