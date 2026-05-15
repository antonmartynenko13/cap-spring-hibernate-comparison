package com.example.springdemo.service;

import com.example.springdemo.dto.CreateDepartmentRequest;
import com.example.springdemo.dto.DepartmentDto;
import com.example.springdemo.entity.Department;
import com.example.springdemo.entity.User;
import com.example.springdemo.repository.DepartmentRepository;
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
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public Page<DepartmentDto> findAll(Pageable pageable) {
        return departmentRepository.findAll(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public DepartmentDto findById(UUID id) {
        return departmentRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found: " + id));
    }

    public DepartmentDto create(CreateDepartmentRequest request) {
        Department department = new Department();
        department.setName(request.getName());
        return toDto(departmentRepository.save(department));
    }

    public DepartmentDto update(UUID id, CreateDepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found: " + id));
        department.setName(request.getName());
        return toDto(departmentRepository.save(department));
    }

    public void delete(UUID id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found: " + id);
        }
        departmentRepository.deleteById(id);
    }

    private DepartmentDto toDto(Department department) {
        DepartmentDto dto = new DepartmentDto();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setUserIds(
                department.getUsers().stream()
                        .map(User::getId)
                        .collect(Collectors.toSet())
        );
        return dto;
    }
}
