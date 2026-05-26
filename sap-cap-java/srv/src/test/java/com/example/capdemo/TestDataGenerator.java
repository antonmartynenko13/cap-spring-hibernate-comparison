package com.example.capdemo;

import cds.gen.userdepartmentservice.AssignUserToDepartmentContext;
import cds.gen.userdepartmentservice.Departments;
import cds.gen.userdepartmentservice.GetUsersCountContext;
import cds.gen.userdepartmentservice.UserDepartments;
import cds.gen.userdepartmentservice.Users;

import java.util.UUID;

public class TestDataGenerator {

    public static Users createUser() {
        Users user = Users.create();
        user.setId(UUID.randomUUID().toString());
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setAddressCountry("GERMANY");
        user.setAddressCity("Berlin");
        return user;
    }

    public static Users createUser(String email) {
        Users user = createUser();
        user.setEmail(email);
        return user;
    }

    public static Departments createDepartment() {
        Departments dept = Departments.create();
        dept.setId(UUID.randomUUID().toString());
        dept.setName("Engineering");
        return dept;
    }

    public static UserDepartments createUserDepartment(String userId, String departmentId) {
        UserDepartments link = UserDepartments.create();
        link.setUserId(userId);
        link.setDepartmentId(departmentId);
        return link;
    }

    public static GetUsersCountContext createGetUsersCountContext() {
        return GetUsersCountContext.create();
    }

    public static AssignUserToDepartmentContext createAssignContext(String userId, String departmentId) {
        AssignUserToDepartmentContext ctx = AssignUserToDepartmentContext.create();
        ctx.setUserID(userId);
        ctx.setDepartmentID(departmentId);
        return ctx;
    }
}
