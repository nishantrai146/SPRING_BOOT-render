package com.lit.ims.repository;

import com.lit.ims.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StateRepository extends JpaRepository<State,String> {
}
