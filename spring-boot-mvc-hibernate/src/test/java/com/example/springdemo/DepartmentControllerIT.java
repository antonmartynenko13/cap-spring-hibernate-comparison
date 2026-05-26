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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DepartmentControllerIT {

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
    // GET /api/departments
    // -------------------------------------------------------------------------

    @Test
    void findAll_WhenAuthenticatedAsUser_ShouldReturn200WithPage() throws Exception {
        // given
        departmentRepository.save(TestDataGenerator.createDepartment());

        // when / then
        mockMvc.perform(get("/api/departments")
                        .with(httpBasic("user1", "user1pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void findAll_WhenUnauthenticated_ShouldReturn401() throws Exception {
        // given — no credentials

        // when / then
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // GET /api/departments/{id}
    // -------------------------------------------------------------------------

    @Test
    void findById_WhenDepartmentExists_ShouldReturn200WithDto() throws Exception {
        // given
        var saved = departmentRepository.save(TestDataGenerator.createDepartment());

        // when / then
        mockMvc.perform(get("/api/departments/{id}", saved.getId())
                        .with(httpBasic("user1", "user1pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(saved.getName()));
    }

    @Test
    void findById_WhenDepartmentDoesNotExist_ShouldReturn404() throws Exception {
        // given
        UUID unknownId = UUID.randomUUID();

        // when / then
        mockMvc.perform(get("/api/departments/{id}", unknownId)
                        .with(httpBasic("user1", "user1pass")))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // POST /api/departments
    // -------------------------------------------------------------------------

    @Test
    void create_WhenAdminAndValidRequest_ShouldReturn201WithDto() throws Exception {
        // given
        var request = TestDataGenerator.createDepartmentRequest();

        // when / then
        mockMvc.perform(post("/api/departments")
                        .with(httpBasic("admin1", "admin1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void create_WhenUserRole_ShouldReturn403() throws Exception {
        // given
        var request = TestDataGenerator.createDepartmentRequest();

        // when / then
        mockMvc.perform(post("/api/departments")
                        .with(httpBasic("user1", "user1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_WhenNameIsBlank_ShouldReturn400() throws Exception {
        // given
        var request = TestDataGenerator.createDepartmentRequest();
        request.setName("");

        // when / then
        mockMvc.perform(post("/api/departments")
                        .with(httpBasic("admin1", "admin1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // PUT /api/departments/{id}
    // -------------------------------------------------------------------------

    @Test
    void update_WhenAdminAndValidRequest_ShouldReturn200WithUpdatedName() throws Exception {
        // given
        var saved = departmentRepository.save(TestDataGenerator.createDepartment("Engineering"));
        var request = TestDataGenerator.createDepartmentRequest();
        request.setName("Design");

        // when / then
        mockMvc.perform(put("/api/departments/{id}", saved.getId())
                        .with(httpBasic("admin1", "admin1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Design"));
    }

    @Test
    void update_WhenDepartmentDoesNotExist_ShouldReturn404() throws Exception {
        // given
        UUID unknownId = UUID.randomUUID();
        var request = TestDataGenerator.createDepartmentRequest();

        // when / then
        mockMvc.perform(put("/api/departments/{id}", unknownId)
                        .with(httpBasic("admin1", "admin1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // DELETE /api/departments/{id}
    // -------------------------------------------------------------------------

    @Test
    void delete_WhenAdminAndDepartmentExists_ShouldReturn204() throws Exception {
        // given
        var saved = departmentRepository.save(TestDataGenerator.createDepartment());

        // when / then
        mockMvc.perform(delete("/api/departments/{id}", saved.getId())
                        .with(httpBasic("admin1", "admin1pass")))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_WhenUserRole_ShouldReturn403() throws Exception {
        // given
        var saved = departmentRepository.save(TestDataGenerator.createDepartment());

        // when / then
        mockMvc.perform(delete("/api/departments/{id}", saved.getId())
                        .with(httpBasic("user1", "user1pass")))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_WhenDepartmentDoesNotExist_ShouldReturn404() throws Exception {
        // given
        UUID unknownId = UUID.randomUUID();

        // when / then
        mockMvc.perform(delete("/api/departments/{id}", unknownId)
                        .with(httpBasic("admin1", "admin1pass")))
                .andExpect(status().isNotFound());
    }
}
