package com.lit.ims.repository;

import com.lit.ims.entity.TypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TypeMasterRepository extends JpaRepository<TypeMaster, Long> {

    boolean existsByNameAndCompanyIdAndBranchId(String name, Long companyId, Long branchId);

    Optional<TypeMaster> findTopByCompanyIdAndBranchIdOrderByIdDesc(Long companyId, Long branchId);

    List<TypeMaster> findAllByCompanyIdAndBranchId(Long companyId, Long branchId);
}
