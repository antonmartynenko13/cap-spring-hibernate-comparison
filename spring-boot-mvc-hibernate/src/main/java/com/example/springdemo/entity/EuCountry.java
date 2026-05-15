package com.example.springdemo.entity;

/**
 * EU member states (27 members as of 2024).
 * Stored as a VARCHAR via @Enumerated(EnumType.STRING) on Address.country.
 *
 * CAP equivalent: type EuCountry : String enum { ... } in data-model.cds.
 * CAP automatically enforces that only declared enum values are accepted (@assert.range).
 */
public enum EuCountry {
    AUSTRIA,
    BELGIUM,
    BULGARIA,
    CROATIA,
    CYPRUS,
    CZECHIA,
    DENMARK,
    ESTONIA,
    FINLAND,
    FRANCE,
    GERMANY,
    GREECE,
    HUNGARY,
    IRELAND,
    ITALY,
    LATVIA,
    LITHUANIA,
    LUXEMBOURG,
    MALTA,
    NETHERLANDS,
    POLAND,
    PORTUGAL,
    ROMANIA,
    SLOVAKIA,
    SLOVENIA,
    SPAIN,
    SWEDEN
}