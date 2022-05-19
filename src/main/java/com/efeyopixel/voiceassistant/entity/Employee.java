package com.efeyopixel.voiceassistant.entity;

import com.fasterxml.jackson.databind.ser.Serializers;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Employee extends BaseEntity {

    @GeneratedValue
    @Id
    private Long id;
//    @ManyToOne(fetch = FetchType.EAGER,targetEntity = Client.class)
//    private String clientNumber;
    private String fullName;
    private String firstName;
    private String lastName;
    private PayType payType;
    private Double pay;
    private Integer vacationHours;
    private Integer overtimeHours;
    private String modeOfPay;



}
