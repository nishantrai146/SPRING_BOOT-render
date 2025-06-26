package com.lit.ims.repository;

import com.lit.ims.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository  extends JpaRepository<Company,Long> {
    Optional<Company> findByCode(String code);

}
