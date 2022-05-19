package com.efeyopixel.voiceassistant.repository;

import com.efeyopixel.voiceassistant.entity.Payperiod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayperiodRepository extends JpaRepository<Payperiod, Long> {
}
