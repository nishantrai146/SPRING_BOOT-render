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
    @Query("SELECT u FROM User u JOIN u.branches b WHERE u.role IN :roles AND u.company.id = :companyId AND b.id = :branchId")
    List<User> findByRoleInAndCompanyIdAndBranchId(@Param("roles") List<Role> roles,
                                                   @Param("companyId") Long companyId,
                                                   @Param("branchId") Long branchId);
    Optional<User> findFirstByRoleAndDepartment(Role role, String department);

    @Query("""
           SELECT u FROM User u
           JOIN u.company c
           JOIN u.branches b
           WHERE u.role       = :role
             AND u.department = :department
             AND c.id         = :companyId
             AND b.id         = :branchId
           """)
    Optional<User> findFirstApprover(@Param("role") Role role,
                                     @Param("department") String department,
                                     @Param("companyId") Long companyId,
                                     @Param("branchId")  Long branchId);

    @Query("""
    SELECT u FROM User u
    JOIN u.company c
    JOIN u.branches b
    WHERE u.role = :role
      AND LOWER(u.department) = LOWER(:department)
      AND c.id = :companyId
      AND b.id = :branchId
""")
    Optional<User> findFirstApproverIgnoreCase(@Param("role") Role role,
                                               @Param("department") String department,
                                               @Param("companyId") Long companyId,
                                               @Param("branchId") Long branchId);




}
