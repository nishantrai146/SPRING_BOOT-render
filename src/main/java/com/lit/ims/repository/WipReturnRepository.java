package com.lit.ims.repository;

import com.lit.ims.entity.WipReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WipReturnRepository extends JpaRepository<WipReturn, Long> {
    List<WipReturn> findTop10ByCompanyIdAndBranchIdOrderByCreatedAtDesc(Long companyId, Long branchId);
    @Query("SELECT COUNT(w) FROM WipReturn w WHERE LOWER(w.returnType) = 'defective material' AND w.companyId = :companyId AND w.branchId = :branchId")
    long countDefectiveReturns(@Param("companyId") Long companyId, @Param("branchId") Long branchId);


}
