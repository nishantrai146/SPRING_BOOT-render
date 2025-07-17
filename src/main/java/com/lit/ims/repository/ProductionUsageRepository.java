package com.lit.ims.repository;

import com.lit.ims.entity.ProductionUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionUsageRepository extends JpaRepository<ProductionUsage,Long> {

}
