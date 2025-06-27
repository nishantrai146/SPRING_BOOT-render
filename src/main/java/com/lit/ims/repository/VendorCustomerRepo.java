package com.lit.ims.repository;

import com.lit.ims.entity.VendorCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendorCustomerRepo  extends JpaRepository<VendorCustomer,Long> {
    List<VendorCustomer> findByCompanyIdAndBranchId(Long companyId, Long branchId);
}
