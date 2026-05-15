package com.example.springdemo.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class Address {

    // Stored as VARCHAR (enum name). NULL is allowed – address is optional.
    // CAP equivalent: country : EuCountry (type EuCountry : String enum in data-model.cds)
    @Enumerated(EnumType.STRING)
    private EuCountry country;
    private String city;

    public Address() {}

    public Address(EuCountry country, String city) {
        this.country = country;
        this.city = city;
    }

    public EuCountry getCountry() { return country; }
    public void setCountry(EuCountry country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}
