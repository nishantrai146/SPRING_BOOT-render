package com.lit.ims.repository;

import com.lit.ims.entity.WipReturn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WipReturnRepository extends JpaRepository<WipReturn, Long> {
    List<WipReturn> findTop10ByCompanyIdAndBranchIdOrderByCreatedAtDesc(Long companyId, Long branchId);

}
