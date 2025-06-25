package com.lit.ims.repository;

import com.lit.ims.entity.GroupMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupMasterRepository extends JpaRepository<GroupMaster,Long> {

    boolean existsByName(String name);
    Optional<GroupMaster> findTopByOrderByIdDesc();


}
