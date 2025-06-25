package com.lit.ims.repository;

import com.lit.ims.entity.VendorCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorCustomerRepo  extends JpaRepository<VendorCustomer,Long> {
}
