package com.example.springdemo;

import com.example.springdemo.dto.AssignDepartmentRequest;
import com.example.springdemo.dto.CreateUserRequest;
import com.example.springdemo.dto.UpdateUserRequest;
import com.example.springdemo.dto.UserDto;
import com.example.springdemo.entity.Department;
import com.example.springdemo.entity.User;
import com.example.springdemo.repository.DepartmentRepository;
import com.example.springdemo.repository.UserRepository;
import com.example.springdemo.service.UserService;
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
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private UserService userService;

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    void findById_WhenUserExists_ShouldReturnDto() {
        // given
        User user = TestDataGenerator.createUser();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // when
        UserDto result = userService.findById(user.getId());

        // then
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
        assertThat(result.getCountry()).isEqualTo("GERMANY");
        assertThat(result.getCity()).isEqualTo("Berlin");
    }

    @Test
    void findById_WhenUserDoesNotExist_ShouldThrow404() {
        // given
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.findById(id))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(NOT_FOUND);
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Test
    void findAll_WhenUsersExist_ShouldReturnMappedPage() {
        // given
        User user = TestDataGenerator.createUser();
        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));

        // when
        var result = userService.findAll(Pageable.unpaged());

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo(user.getEmail());
    }

    // -------------------------------------------------------------------------
    // create
    // -------------------------------------------------------------------------

    @Test
    void create_WhenEmailIsNew_ShouldSaveAndReturnDto() {
        // given
        CreateUserRequest request = TestDataGenerator.createUserRequest();
        User savedUser = TestDataGenerator.createUser();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        UserDto result = userService.create(request);

        // then
        assertThat(result.getEmail()).isEqualTo(savedUser.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_WhenEmailAlreadyExists_ShouldThrow409() {
        // given
        CreateUserRequest request = TestDataGenerator.createUserRequest();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(CONFLICT);

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_WhenCountryIsBlank_ShouldSaveUserWithNullCountry() {
        // given
        CreateUserRequest request = TestDataGenerator.createUserRequest();
        request.setCountry("");
        User savedUser = TestDataGenerator.createUser();
        savedUser.setAddress(null);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        UserDto result = userService.create(request);

        // then
        assertThat(result.getCountry()).isNull();
        verify(userRepository).save(any(User.class));
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Test
    void update_WhenUserExistsAndEmailFree_ShouldUpdateAndReturnDto() {
        // given
        User existing = TestDataGenerator.createUser();
        UpdateUserRequest request = TestDataGenerator.updateUserRequest();
        User updated = TestDataGenerator.createUser(request.getEmail());

        when(userRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailAndIdNot(request.getEmail(), existing.getId())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updated);

        // when
        UserDto result = userService.update(existing.getId(), request);

        // then
        assertThat(result.getEmail()).isEqualTo(request.getEmail());
        verify(userRepository).save(existing);
    }

    @Test
    void update_WhenUserDoesNotExist_ShouldThrow404() {
        // given
        UUID id = UUID.randomUUID();
        UpdateUserRequest request = TestDataGenerator.updateUserRequest();

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.update(id, request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(NOT_FOUND);
    }

    @Test
    void update_WhenEmailTakenByAnotherUser_ShouldThrow409() {
        // given
        User existing = TestDataGenerator.createUser();
        UpdateUserRequest request = TestDataGenerator.updateUserRequest();

        when(userRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailAndIdNot(request.getEmail(), existing.getId())).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> userService.update(existing.getId(), request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(CONFLICT);

        verify(userRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Test
    void delete_WhenUserExists_ShouldDeleteById() {
        // given
        UUID id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(true);

        // when
        userService.delete(id);

        // then
        verify(userRepository).deleteById(id);
    }

    @Test
    void delete_WhenUserDoesNotExist_ShouldThrow404() {
        // given
        UUID id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(false);

        // when / then
        assertThatThrownBy(() -> userService.delete(id))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(NOT_FOUND);

        verify(userRepository, never()).deleteById(any());
    }

    // -------------------------------------------------------------------------
    // count
    // -------------------------------------------------------------------------

    @Test
    void count_ShouldReturnRepositoryCount() {
        // given
        when(userRepository.count()).thenReturn(42L);

        // when
        long result = userService.count();

        // then
        assertThat(result).isEqualTo(42L);
    }

    // -------------------------------------------------------------------------
    // assignDepartment
    // -------------------------------------------------------------------------

    @Test
    void assignDepartment_WhenBothExist_ShouldAddDepartmentAndReturnDto() {
        // given
        User user = TestDataGenerator.createUser();
        Department department = TestDataGenerator.createDepartment();
        AssignDepartmentRequest request = TestDataGenerator.assignDepartmentRequest(department.getId());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(departmentRepository.findById(department.getId())).thenReturn(Optional.of(department));
        when(userRepository.save(user)).thenReturn(user);

        // when
        UserDto result = userService.assignDepartment(user.getId(), request);

        // then
        assertThat(result.getDepartmentIds()).contains(department.getId());
        verify(userRepository).save(user);
    }

    @Test
    void assignDepartment_WhenUserDoesNotExist_ShouldThrow404() {
        // given
        UUID userId = UUID.randomUUID();
        AssignDepartmentRequest request = TestDataGenerator.assignDepartmentRequest(UUID.randomUUID());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.assignDepartment(userId, request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(NOT_FOUND);
    }

    @Test
    void assignDepartment_WhenDepartmentDoesNotExist_ShouldThrow404() {
        // given
        User user = TestDataGenerator.createUser();
        UUID departmentId = UUID.randomUUID();
        AssignDepartmentRequest request = TestDataGenerator.assignDepartmentRequest(departmentId);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.assignDepartment(user.getId(), request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(NOT_FOUND);
    }
}
