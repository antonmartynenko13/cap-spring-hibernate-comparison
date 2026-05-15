package com.example.springdemo.controller;

import com.example.springdemo.dto.AssignDepartmentRequest;
import com.example.springdemo.dto.CreateUserRequest;
import com.example.springdemo.dto.UpdateUserRequest;
import com.example.springdemo.dto.UserDto;
import com.example.springdemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Page<UserDto> findAll(@PageableDefault(size = 20, sort = "lastName") Pageable pageable) {
        return userService.findAll(pageable);
    }

    // IMPORTANT: /count must be declared BEFORE /{id}.
    // If /{id} comes first, Spring tries to parse "count" as a UUID and returns 400.
    // CAP equivalent: function getUsersCount() returns Integer
    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public long count() {
        return userService.count();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public UserDto findById(@PathVariable UUID id) {
        return userService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable UUID id) {
        userService.delete(id);
    }

    // OData Action equivalent: assignUserToDepartment(userID, departmentID)
    @PostMapping("/{id}/assign-department")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto assignDepartment(@PathVariable UUID id,
                                    @Valid @RequestBody AssignDepartmentRequest request) {
        return userService.assignDepartment(id, request);
    }
}
