package com.lit.ims.repository;

import com.lit.ims.entity.GroupMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMasterRepository extends JpaRepository<GroupMaster, Long> {

    Optional<GroupMaster> findTopByCompanyIdAndBranchIdOrderByIdDesc(Long companyId, Long branchId);

    Optional<GroupMaster> findByIdAndCompanyIdAndBranchId(Long id, Long companyId, Long branchId);

    List<GroupMaster> findByCompanyIdAndBranchId(Long companyId, Long branchId);

    boolean existsByNameAndCompanyIdAndBranchId(String name, Long companyId, Long branchId);
}
