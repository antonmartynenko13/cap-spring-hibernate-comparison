package com.example.springdemo;

import com.example.springdemo.dto.CreateDepartmentRequest;
import com.example.springdemo.dto.DepartmentDto;
import com.example.springdemo.entity.Department;
import com.example.springdemo.repository.DepartmentRepository;
import com.example.springdemo.service.DepartmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    void findById_WhenDepartmentExists_ShouldReturnDto() {
        // given
        Department department = TestDataGenerator.createDepartment();
        when(departmentRepository.findById(department.getId())).thenReturn(Optional.of(department));

        // when
        DepartmentDto result = departmentService.findById(department.getId());

        // then
        assertThat(result.getId()).isEqualTo(department.getId());
        assertThat(result.getName()).isEqualTo(department.getName());
    }

    @Test
    void findById_WhenDepartmentDoesNotExist_ShouldThrow404() {
        // given
        UUID id = UUID.randomUUID();
        when(departmentRepository.findById(id)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> departmentService.findById(id))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(NOT_FOUND);
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Test
    void findAll_WhenDepartmentsExist_ShouldReturnMappedPage() {
        // given
        Department department = TestDataGenerator.createDepartment();
        when(departmentRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(department)));

        // when
        var result = departmentService.findAll(Pageable.unpaged());

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo(department.getName());
    }

    // -------------------------------------------------------------------------
    // create
    // -------------------------------------------------------------------------

    @Test
    void create_WhenRequestIsValid_ShouldSaveAndReturnDto() {
        // given
        CreateDepartmentRequest request = TestDataGenerator.createDepartmentRequest();
        Department saved = TestDataGenerator.createDepartment(request.getName());

        when(departmentRepository.save(any(Department.class))).thenReturn(saved);

        // when
        DepartmentDto result = departmentService.create(request);

        // then
        assertThat(result.getName()).isEqualTo(request.getName());
        verify(departmentRepository).save(any(Department.class));
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Test
    void update_WhenDepartmentExists_ShouldUpdateNameAndReturnDto() {
        // given
        Department existing = TestDataGenerator.createDepartment("Engineering");
        CreateDepartmentRequest request = TestDataGenerator.createDepartmentRequest();
        request.setName("Design");
        Department updated = TestDataGenerator.createDepartment("Design");

        when(departmentRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(departmentRepository.save(existing)).thenReturn(updated);

        // when
        DepartmentDto result = departmentService.update(existing.getId(), request);

        // then
        assertThat(result.getName()).isEqualTo("Design");
        verify(departmentRepository).save(existing);
    }

    @Test
    void update_WhenDepartmentDoesNotExist_ShouldThrow404() {
        // given
        UUID id = UUID.randomUUID();
        CreateDepartmentRequest request = TestDataGenerator.createDepartmentRequest();

        when(departmentRepository.findById(id)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> departmentService.update(id, request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(NOT_FOUND);
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Test
    void delete_WhenDepartmentExists_ShouldDeleteById() {
        // given
        UUID id = UUID.randomUUID();
        when(departmentRepository.existsById(id)).thenReturn(true);

        // when
        departmentService.delete(id);

        // then
        verify(departmentRepository).deleteById(id);
    }

    @Test
    void delete_WhenDepartmentDoesNotExist_ShouldThrow404() {
        // given
        UUID id = UUID.randomUUID();
        when(departmentRepository.existsById(id)).thenReturn(false);

        // when / then
        assertThatThrownBy(() -> departmentService.delete(id))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(NOT_FOUND);

        verify(departmentRepository, never()).deleteById(any());
    }
}
