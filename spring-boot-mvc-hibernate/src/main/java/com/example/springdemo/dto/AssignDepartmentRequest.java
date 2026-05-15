package com.example.springdemo.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AssignDepartmentRequest {

    @NotNull
    private UUID departmentId;

    public UUID getDepartmentId() { return departmentId; }
    public void setDepartmentId(UUID departmentId) { this.departmentId = departmentId; }
}
