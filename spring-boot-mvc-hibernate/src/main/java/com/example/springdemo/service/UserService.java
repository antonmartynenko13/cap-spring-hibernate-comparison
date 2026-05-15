package com.example.springdemo.service;

import com.example.springdemo.dto.AssignDepartmentRequest;
import com.example.springdemo.dto.CreateUserRequest;
import com.example.springdemo.dto.UpdateUserRequest;
import com.example.springdemo.dto.UserDto;
import com.example.springdemo.entity.Address;
import com.example.springdemo.entity.Department;
import com.example.springdemo.entity.EuCountry;
import com.example.springdemo.entity.User;
import com.example.springdemo.repository.DepartmentRepository;
import com.example.springdemo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public UserService(UserRepository userRepository, DepartmentRepository departmentRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public UserDto findById(UUID id) {
        return userRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
    }

    public UserDto create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email already in use: " + request.getEmail());
        }
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setAddress(new Address(toEuCountry(request.getCountry()), request.getCity()));
        return toDto(userRepository.save(user));
    }

    public UserDto update(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
        if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email already in use: " + request.getEmail());
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setAddress(new Address(toEuCountry(request.getCountry()), request.getCity()));
        return toDto(userRepository.save(user));
    }

    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long count() {
        return userRepository.count();
    }

    // OData Action equivalent: assign a user to a department
    public UserDto assignDepartment(UUID userId, AssignDepartmentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Department not found: " + request.getDepartmentId()));
        user.getDepartments().add(department);
        return toDto(userRepository.save(user));
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        if (user.getAddress() != null) {
            EuCountry country = user.getAddress().getCountry();
            dto.setCountry(country != null ? country.name() : null);
            dto.setCity(user.getAddress().getCity());
        }
        dto.setDepartmentIds(
                user.getDepartments().stream()
                        .map(Department::getId)
                        .collect(Collectors.toSet())
        );
        return dto;
    }

    // Converts a nullable/blank country string from the request DTO to an EuCountry enum.
    // Null/blank → null (address.country is optional).
    // Invalid values are rejected upstream by @EuCountry on the DTO field, so valueOf is safe here.
    private EuCountry toEuCountry(String country) {
        if (country == null || country.isBlank()) return null;
        return EuCountry.valueOf(country.trim().toUpperCase());
    }
}
