package com.lit.ims.repository;

import com.lit.ims.entity.VendorCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VendorCustomerRepo extends JpaRepository<VendorCustomer, Long> {

    List<VendorCustomer> findByCompanyIdAndBranchId(Long companyId, Long branchId);

    @Query("SELECT MAX(vc.code) FROM VendorCustomer vc WHERE vc.type = :type AND vc.companyId = :companyId AND vc.branchId = :branchId")
    String findMaxCodeByTypeAndCompanyIdAndBranchId(
            @Param("type") String type,
            @Param("companyId") Long companyId,
            @Param("branchId") Long branchId
    );

    List<VendorCustomer> findByTypeAndCompanyIdAndBranchId(String type, Long companyId, Long branchId);

}
