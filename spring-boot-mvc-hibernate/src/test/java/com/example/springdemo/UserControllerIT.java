package com.example.springdemo;

import com.example.springdemo.repository.DepartmentRepository;
import com.example.springdemo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        departmentRepository.deleteAll();
    }

    // -------------------------------------------------------------------------
    // GET /api/users
    // -------------------------------------------------------------------------

    @Test
    void findAll_WhenAuthenticatedAsUser_ShouldReturn200WithPage() throws Exception {
        // given
        userRepository.save(TestDataGenerator.createUser());

        // when / then
        mockMvc.perform(get("/api/users")
                        .with(httpBasic("user1", "user1pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void findAll_WhenUnauthenticated_ShouldReturn401() throws Exception {
        // given — no credentials

        // when / then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // GET /api/users/count
    // -------------------------------------------------------------------------

    @Test
    void count_WhenAuthenticatedAsUser_ShouldReturnCount() throws Exception {
        // given
        userRepository.save(TestDataGenerator.createUser());

        // when / then
        mockMvc.perform(get("/api/users/count")
                        .with(httpBasic("user1", "user1pass")))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    // -------------------------------------------------------------------------
    // GET /api/users/{id}
    // -------------------------------------------------------------------------

    @Test
    void findById_WhenUserExists_ShouldReturn200WithDto() throws Exception {
        // given
        var saved = userRepository.save(TestDataGenerator.createUser());

        // when / then
        mockMvc.perform(get("/api/users/{id}", saved.getId())
                        .with(httpBasic("user1", "user1pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(saved.getEmail()))
                .andExpect(jsonPath("$.country").value("GERMANY"))
                .andExpect(jsonPath("$.city").value("Berlin"));
    }

    @Test
    void findById_WhenUserDoesNotExist_ShouldReturn404() throws Exception {
        // given
        UUID unknownId = UUID.randomUUID();

        // when / then
        mockMvc.perform(get("/api/users/{id}", unknownId)
                        .with(httpBasic("user1", "user1pass")))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // POST /api/users
    // -------------------------------------------------------------------------

    @Test
    void create_WhenAdminAndValidRequest_ShouldReturn201WithDto() throws Exception {
        // given
        var request = TestDataGenerator.createUserRequest();

        // when / then
        mockMvc.perform(post("/api/users")
                        .with(httpBasic("admin1", "admin1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.firstName").value(request.getFirstName()))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void create_WhenUserRole_ShouldReturn403() throws Exception {
        // given
        var request = TestDataGenerator.createUserRequest();

        // when / then
        mockMvc.perform(post("/api/users")
                        .with(httpBasic("user1", "user1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_WhenEmailAlreadyExists_ShouldReturn409() throws Exception {
        // given
        userRepository.save(TestDataGenerator.createUser());
        var request = TestDataGenerator.createUserRequest();

        // when / then
        mockMvc.perform(post("/api/users")
                        .with(httpBasic("admin1", "admin1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void create_WhenFirstNameIsBlank_ShouldReturn400() throws Exception {
        // given
        var request = TestDataGenerator.createUserRequest();
        request.setFirstName("");

        // when / then
        mockMvc.perform(post("/api/users")
                        .with(httpBasic("admin1", "admin1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WhenEmailIsInvalid_ShouldReturn400() throws Exception {
        // given
        var request = TestDataGenerator.createUserRequest();
        request.setEmail("not-an-email");

        // when / then
        mockMvc.perform(post("/api/users")
                        .with(httpBasic("admin1", "admin1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WhenCountryIsInvalid_ShouldReturn400() throws Exception {
        // given
        var request = TestDataGenerator.createUserRequest();
        request.setCountry("NARNIA");

        // when / then
        mockMvc.perform(post("/api/users")
                        .with(httpBasic("admin1", "admin1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // PUT /api/users/{id}
    // -------------------------------------------------------------------------

    @Test
    void update_WhenAdminAndValidRequest_ShouldReturn200WithUpdatedDto() throws Exception {
        // given
        var saved = userRepository.save(TestDataGenerator.createUser());
        var request = TestDataGenerator.updateUserRequest();

        // when / then
        mockMvc.perform(put("/api/users/{id}", saved.getId())
                        .with(httpBasic("admin1", "admin1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.country").value("FRANCE"));
    }

    @Test
    void update_WhenUserDoesNotExist_ShouldReturn404() throws Exception {
        // given
        UUID unknownId = UUID.randomUUID();
        var request = TestDataGenerator.updateUserRequest();

        // when / then
        mockMvc.perform(put("/api/users/{id}", unknownId)
                        .with(httpBasic("admin1", "admin1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // DELETE /api/users/{id}
    // -------------------------------------------------------------------------

    @Test
    void delete_WhenAdminAndUserExists_ShouldReturn204() throws Exception {
        // given
        var saved = userRepository.save(TestDataGenerator.createUser());

        // when / then
        mockMvc.perform(delete("/api/users/{id}", saved.getId())
                        .with(httpBasic("admin1", "admin1pass")))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_WhenUserRole_ShouldReturn403() throws Exception {
        // given
        var saved = userRepository.save(TestDataGenerator.createUser());

        // when / then
        mockMvc.perform(delete("/api/users/{id}", saved.getId())
                        .with(httpBasic("user1", "user1pass")))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_WhenUserDoesNotExist_ShouldReturn404() throws Exception {
        // given
        UUID unknownId = UUID.randomUUID();

        // when / then
        mockMvc.perform(delete("/api/users/{id}", unknownId)
                        .with(httpBasic("admin1", "admin1pass")))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // POST /api/users/{id}/assign-department
    // -------------------------------------------------------------------------

    @Test
    void assignDepartment_WhenBothExist_ShouldReturn200WithDepartmentInDto() throws Exception {
        // given
        var user = userRepository.save(TestDataGenerator.createUser());
        var department = departmentRepository.save(TestDataGenerator.createDepartment());
        var request = TestDataGenerator.assignDepartmentRequest(department.getId());

        // when / then
        mockMvc.perform(post("/api/users/{id}/assign-department", user.getId())
                        .with(httpBasic("admin1", "admin1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departmentIds", contains(department.getId().toString())));
    }

    @Test
    void assignDepartment_WhenDepartmentDoesNotExist_ShouldReturn404() throws Exception {
        // given
        var user = userRepository.save(TestDataGenerator.createUser());
        var request = TestDataGenerator.assignDepartmentRequest(UUID.randomUUID());

        // when / then
        mockMvc.perform(post("/api/users/{id}/assign-department", user.getId())
                        .with(httpBasic("admin1", "admin1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
