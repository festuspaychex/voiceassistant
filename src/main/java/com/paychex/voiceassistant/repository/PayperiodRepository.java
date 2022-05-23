package com.paychex.voiceassistant.repository;

import com.paychex.voiceassistant.entity.Payperiod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayperiodRepository extends JpaRepository<Payperiod, Long> {
}
