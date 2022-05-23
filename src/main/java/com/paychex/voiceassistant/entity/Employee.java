package com.paychex.voiceassistant.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Employee extends BaseEntity {

    @GeneratedValue
    @Id
    private Long id;
    private String fullName;
    private String firstName;
    private String lastName;
    private PayType payType;
    private Double pay;
    private Integer vacationHours;
    private Integer overtimeHours;
    private String modeOfPay;
}
