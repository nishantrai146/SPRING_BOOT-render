package com.lit.ims.repository;

import com.lit.ims.entity.PagePermission;
import com.lit.ims.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagePermissionRepository extends JpaRepository<PagePermission,Long> {
    List<PagePermission> findByUser(User user);
}
