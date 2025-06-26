package com.lit.ims.repository;

import com.lit.ims.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    Optional<Branch> findByCodeAndCompanyId(String code, Long companyId);

    List<Branch> findByCompanyId(Long companyId);
}
