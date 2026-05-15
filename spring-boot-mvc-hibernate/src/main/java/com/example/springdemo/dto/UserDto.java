package com.example.springdemo.dto;

import java.util.Set;
import java.util.UUID;

public class UserDto {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String country;
    private String city;
    // Flat set of department IDs avoids recursive serialisation of User <-> Department
    private Set<UUID> departmentIds;

    public UserDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public Set<UUID> getDepartmentIds() { return departmentIds; }
    public void setDepartmentIds(Set<UUID> departmentIds) { this.departmentIds = departmentIds; }
}
