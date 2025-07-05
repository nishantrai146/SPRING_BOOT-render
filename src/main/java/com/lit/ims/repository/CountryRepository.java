package com.lit.ims.repository;

import com.lit.ims.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, String> {}
