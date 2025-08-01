package com.lit.ims.repository;

import com.lit.ims.entity.Item;
import com.lit.ims.entity.MaterialReceiptItem;
import com.lit.ims.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByCompanyIdAndBranchId(Long companyId, Long branchId);

    Optional<Item> findByIdAndCompanyIdAndBranchId(Long id, Long companyId, Long branchId);
    boolean existsByCodeAndCompanyIdAndBranchIdAndIdNot(String code, Long companyId, Long branchId, Long id);
//    Optional<Item> findByCodeAndCompanyIdAndBranchId(String itemCode, Long companyId, Long branchId);


    boolean existsByCodeAndCompanyIdAndBranchId(String code, Long companyId, Long branchId);
    Optional<Item> findByCode(String code);
    Optional<Item> findByCodeAndCompanyIdAndBranchId(String code, Long companyId, Long branchId);


}
