package com.lit.ims.repository;

import com.lit.ims.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City,Long> {
}
