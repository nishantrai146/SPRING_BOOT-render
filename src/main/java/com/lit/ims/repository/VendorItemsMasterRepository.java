package com.lit.ims.repository;

import com.lit.ims.entity.VendorItemsMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorItemsMasterRepository extends JpaRepository<VendorItemsMaster, Long> {

    List<VendorItemsMaster> findByCompanyIdAndBranchId(Long companyId, Long branchId);

    Optional<VendorItemsMaster> findByIdAndCompanyIdAndBranchId(Long id, Long companyId, Long branchId);

    boolean existsByVendorCodeAndItemCodeAndCompanyIdAndBranchId(String vendorCode, String itemCode, Long companyId, Long branchId);

    List<VendorItemsMaster> findByVendorCodeAndCompanyIdAndBranchId(String vendorCode,Long companyId,Long branchId);
    Optional<VendorItemsMaster> findByVendorCodeAndItemCode(String vendorCode,
                                                            String itemCode);
}
