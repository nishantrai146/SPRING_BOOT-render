package com.lit.ims.repository;

import com.lit.ims.entity.Role;
import com.lit.ims.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRoleIn(List<Role> roles);
    @Query("SELECT u FROM User u JOIN u.branches b WHERE u.role IN :roles AND u.company.id = :companyId AND b.id = :branchId")
    List<User> findByRoleInAndCompanyIdAndBranchId(@Param("roles") List<Role> roles,
                                                   @Param("companyId") Long companyId,
                                                   @Param("branchId") Long branchId);

}
