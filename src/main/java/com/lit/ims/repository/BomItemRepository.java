package com.lit.ims.repository;

import com.lit.ims.entity.BomItem;
import com.lit.ims.entity.BOM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BomItemRepository extends JpaRepository<BomItem, Long> {
    List<BomItem> findByBom(BOM bom);
}
