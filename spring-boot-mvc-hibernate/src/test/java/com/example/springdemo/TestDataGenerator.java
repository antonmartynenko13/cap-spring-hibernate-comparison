package com.example.springdemo;

import com.example.springdemo.dto.AssignDepartmentRequest;
import com.example.springdemo.dto.CreateDepartmentRequest;
import com.example.springdemo.dto.CreateUserRequest;
import com.example.springdemo.dto.UpdateUserRequest;
import com.example.springdemo.entity.Address;
import com.example.springdemo.entity.Department;
import com.example.springdemo.entity.EuCountry;
import com.example.springdemo.entity.User;

import java.lang.reflect.Field;
import java.util.UUID;

public class TestDataGenerator {

    public static User createUser() {
        User user = new User();
        setId(user, UUID.randomUUID());
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setAddress(new Address(EuCountry.GERMANY, "Berlin"));
        return user;
    }

    public static User createUser(String email) {
        User user = createUser();
        user.setEmail(email);
        return user;
    }

    public static Department createDepartment() {
        Department dept = new Department();
        setId(dept, UUID.randomUUID());
        dept.setName("Engineering");
        return dept;
    }

    public static Department createDepartment(String name) {
        Department dept = createDepartment();
        dept.setName(name);
        return dept;
    }

    public static CreateUserRequest createUserRequest() {
        CreateUserRequest req = new CreateUserRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john.doe@example.com");
        req.setCountry("GERMANY");
        req.setCity("Berlin");
        return req;
    }

    public static UpdateUserRequest updateUserRequest() {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setFirstName("Jane");
        req.setLastName("Doe");
        req.setEmail("jane.doe@example.com");
        req.setCountry("FRANCE");
        req.setCity("Paris");
        return req;
    }

    public static CreateDepartmentRequest createDepartmentRequest() {
        CreateDepartmentRequest req = new CreateDepartmentRequest();
        req.setName("Engineering");
        return req;
    }

    public static AssignDepartmentRequest assignDepartmentRequest(UUID departmentId) {
        AssignDepartmentRequest req = new AssignDepartmentRequest();
        req.setDepartmentId(departmentId);
        return req;
    }

    private static void setId(Object entity, UUID id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Could not set id on " + entity.getClass().getSimpleName(), e);
        }
    }
}
