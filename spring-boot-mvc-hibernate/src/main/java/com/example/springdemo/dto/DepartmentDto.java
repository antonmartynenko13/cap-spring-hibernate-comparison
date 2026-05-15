package com.example.springdemo.dto;

import java.util.Set;
import java.util.UUID;

public class DepartmentDto {

    private UUID id;
    private String name;
    private Set<UUID> userIds;

    public DepartmentDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Set<UUID> getUserIds() { return userIds; }
    public void setUserIds(Set<UUID> userIds) { this.userIds = userIds; }
}
