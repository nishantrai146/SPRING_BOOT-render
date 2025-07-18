package com.lit.ims.repository;

import com.lit.ims.entity.City;
import com.lit.ims.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CityRepository extends JpaRepository<City,Long> {
    List<City> findByState(State state);

}
