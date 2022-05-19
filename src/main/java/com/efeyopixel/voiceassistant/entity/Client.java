package com.efeyopixel.voiceassistant.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Client extends BaseEntity {

    @Id
    private String clientNumber;
    private String companyName;
    private String address;
    private String contactPerson;
    private String contactEmail;

    @OneToMany(fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonProperty("employees")
    private List<Employee> employees = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonProperty("payperiods")
    private List<Payperiod> payperiods = new ArrayList<>();

}
