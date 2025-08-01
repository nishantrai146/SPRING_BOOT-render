package com.lit.ims.repository;

import com.lit.ims.entity.BOM;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BomRepository extends JpaRepository<BOM, Long> {
    List<BOM> findByCompanyIdAndBranchId(Long companyId, Long branchId);
    boolean existsByCodeAndCompanyIdAndBranchId(String code, Long companyId, Long branchId);
    Optional<BOM> findByCode(String code);

}
