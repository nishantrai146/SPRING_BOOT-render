package com.lit.ims.repository;

import com.lit.ims.entity.Country;
import com.lit.ims.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StateRepository extends JpaRepository<State, Long> {
    Optional<State> findByIso2AndCountry(String iso2, Country country);
    List<State> findByCountry(Country country);
}

