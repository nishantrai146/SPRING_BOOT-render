package com.lit.ims.repository;

import com.lit.ims.entity.ProductionUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionUsageRepository extends JpaRepository<ProductionUsage,Long> {

    List<ProductionUsage> findByCompanyIdAndBranchId(Long companyId,Long branchId);
    Optional<ProductionUsage> findByIdAndCompanyIdAndBranchId(Long id, Long companyId, Long branchId);


}
