package com.efeyopixel.voiceassistant.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Payperiod extends BaseEntity {
    @GeneratedValue
    @Id
    private Long payPeriodId;
    private LocalDate checkDate;
    private LocalDate startDate;
    private LocalDate endDate;
//    @OneToOne
//    private Client client;
//    @OneToMany(fetch = FetchType.LAZY)
//    @LazyCollection(LazyCollectionOption.FALSE)
//    @JsonProperty("payrollWorkers")
//    private List<Employee> payrollWorkers = new ArrayList<>();


}
