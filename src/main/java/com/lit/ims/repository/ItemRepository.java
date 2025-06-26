package com.lit.ims.repository;

import com.lit.ims.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item,Long> {
    List<Item> findByCompanyIdAndBranchId(Long companyId, Long branchId);

    Optional<Item> findByIdAndCompanyIdAndBranchId(Long id, Long companyId, Long branchId);

    boolean existsByCodeAndCompanyIdAndBranchId(String code, Long companyId, Long branchId);
}
