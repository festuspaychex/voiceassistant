package com.paychex.voiceassistant.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
public class Payperiod extends BaseEntity {
    @GeneratedValue
    @Id
    private Long payPeriodId;
    private LocalDate checkDate;
    private LocalDate startDate;
    private LocalDate endDate;
}
