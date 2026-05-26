package com.example.capdemo;

import cds.gen.userdepartmentservice.Departments_;
import cds.gen.userdepartmentservice.UserDepartments_;
import cds.gen.userdepartmentservice.Users_;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.cds.ql.Delete;
import com.sap.cds.ql.Insert;
import com.sap.cds.services.persistence.PersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserServiceIT {

    private static final String BASE = "/odata/v4/UserDepartmentService";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersistenceService db;

    @BeforeEach
    void setUp() {
        db.run(Delete.from(UserDepartments_.class));
        db.run(Delete.from(Users_.class));
        db.run(Delete.from(Departments_.class));
    }

    // -------------------------------------------------------------------------
    // GET /Users
    // -------------------------------------------------------------------------

    @Test
    void findAllUsers_WhenAuthenticatedAsUser_ShouldReturn200WithResults() throws Exception {
        // given
        insertUser(UUID.randomUUID().toString(), "john.doe@example.com", "John", "Doe");

        // when / then
        mockMvc.perform(get(BASE + "/Users")
                        .with(httpBasic("user1", "user1pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value", hasSize(1)));
    }

    @Test
    void findAllUsers_WhenUnauthenticated_ShouldReturn401() throws Exception {
        // given — no credentials

        // when / then
        mockMvc.perform(get(BASE + "/Users"))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // GET /Users({id})
    // -------------------------------------------------------------------------

    @Test
    void findUserById_WhenUserExists_ShouldReturn200WithDto() throws Exception {
        // given
        String id = UUID.randomUUID().toString();
        insertUser(id, "john.doe@example.com", "John", "Doe");

        // when / then
        mockMvc.perform(get(BASE + "/Users({id})", id)
                        .with(httpBasic("user1", "user1pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void findUserById_WhenUserDoesNotExist_ShouldReturn404() throws Exception {
        // given
        String unknownId = UUID.randomUUID().toString();

        // when / then
        mockMvc.perform(get(BASE + "/Users({id})", unknownId)
                        .with(httpBasic("user1", "user1pass")))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // POST /Users
    // -------------------------------------------------------------------------

    @Test
    void createUser_WhenAdminAndValidRequest_ShouldReturn201WithDto() throws Exception {
        // given
        var body = Map.of(
                "firstName", "John",
                "lastName", "Doe",
                "email", "john.doe@example.com",
                "address_country", "GERMANY",
                "address_city", "Berlin"
        );

        // when / then
        mockMvc.perform(post(BASE + "/Users")
                        .with(httpBasic("admin1", "admin1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.ID").isNotEmpty());
    }

    @Test
    void createUser_WhenUserRole_ShouldReturn403() throws Exception {
        // given
        var body = Map.of("firstName", "John", "lastName", "Doe", "email", "j@example.com");

        // when / then
        mockMvc.perform(post(BASE + "/Users")
                        .with(httpBasic("user1", "user1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_WhenFirstNameIsBlank_ShouldReturn400() throws Exception {
        // given
        var body = Map.of("firstName", "", "lastName", "Doe", "email", "j@example.com");

        // when / then
        mockMvc.perform(post(BASE + "/Users")
                        .with(httpBasic("admin1", "admin1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_WhenCountryIsInvalid_ShouldReturn400() throws Exception {
        // given
        var body = Map.of(
                "firstName", "John", "lastName", "Doe",
                "email", "j@example.com", "address_country", "NARNIA"
        );

        // when / then
        mockMvc.perform(post(BASE + "/Users")
                        .with(httpBasic("admin1", "admin1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // PATCH /Users({id})
    // -------------------------------------------------------------------------

    @Test
    void updateUser_WhenAdminAndValidRequest_ShouldReturn200WithUpdatedDto() throws Exception {
        // given
        String id = UUID.randomUUID().toString();
        insertUser(id, "john.doe@example.com", "John", "Doe");
        var body = Map.of("lastName", "Doe-Updated");

        // when / then
        mockMvc.perform(patch(BASE + "/Users({id})", id)
                        .with(httpBasic("admin1", "admin1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Doe-Updated"));
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ShouldReturn404Or400() throws Exception {
        // given
        String unknownId = UUID.randomUUID().toString();

        // when / then
        // CAP OData runtime returns 400 for PATCH on a non-existent entity (not 404 as REST convention).
        mockMvc.perform(patch(BASE + "/Users({id})", unknownId)
                        .with(httpBasic("admin1", "admin1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("lastName", "X"))))
                .andExpect(status().is4xxClientError());
    }

    // -------------------------------------------------------------------------
    // DELETE /Users({id})
    // -------------------------------------------------------------------------

    @Test
    void deleteUser_WhenAdminAndUserExists_ShouldReturn204() throws Exception {
        // given
        String id = UUID.randomUUID().toString();
        insertUser(id, "john.doe@example.com", "John", "Doe");

        // when / then
        mockMvc.perform(delete(BASE + "/Users({id})", id)
                        .with(httpBasic("admin1", "admin1pass")))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_WhenUserRole_ShouldReturn403() throws Exception {
        // given
        String id = UUID.randomUUID().toString();
        insertUser(id, "john.doe@example.com", "John", "Doe");

        // when / then
        mockMvc.perform(delete(BASE + "/Users({id})", id)
                        .with(httpBasic("user1", "user1pass")))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // GET /getUsersCount()
    // -------------------------------------------------------------------------

    @Test
    void getUsersCount_WhenCalled_ShouldReturnCorrectCount() throws Exception {
        // given
        insertUser(UUID.randomUUID().toString(), "a@example.com", "A", "A");
        insertUser(UUID.randomUUID().toString(), "b@example.com", "B", "B");

        // when / then
        mockMvc.perform(get(BASE + "/getUsersCount()")
                        .with(httpBasic("user1", "user1pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(2));
    }

    // -------------------------------------------------------------------------
    // POST /assignUserToDepartment
    // -------------------------------------------------------------------------

    @Test
    void assignUserToDepartment_WhenBothExist_ShouldReturnSuccess() throws Exception {
        // given
        String userId = UUID.randomUUID().toString();
        String deptId = UUID.randomUUID().toString();
        insertUser(userId, "john.doe@example.com", "John", "Doe");
        insertDepartment(deptId, "Engineering");

        var body = Map.of("userID", userId, "departmentID", deptId);

        // when / then
        mockMvc.perform(post(BASE + "/assignUserToDepartment")
                        .with(httpBasic("admin1", "admin1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void assignUserToDepartment_WhenUserDoesNotExist_ShouldReturn404() throws Exception {
        // given
        String deptId = UUID.randomUUID().toString();
        insertDepartment(deptId, "Engineering");

        var body = Map.of("userID", UUID.randomUUID().toString(), "departmentID", deptId);

        // when / then
        mockMvc.perform(post(BASE + "/assignUserToDepartment")
                        .with(httpBasic("admin1", "admin1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void assignUserToDepartment_WhenUserRole_ShouldReturn403() throws Exception {
        // given
        var body = Map.of("userID", UUID.randomUUID().toString(), "departmentID", UUID.randomUUID().toString());

        // when / then
        mockMvc.perform(post(BASE + "/assignUserToDepartment")
                        .with(httpBasic("user1", "user1pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void insertUser(String id, String email, String firstName, String lastName) {
        var user = TestDataGenerator.createUser(email);
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        db.run(Insert.into(Users_.class).entry(user));
    }

    private void insertDepartment(String id, String name) {
        var dept = TestDataGenerator.createDepartment();
        dept.setId(id);
        dept.setName(name);
        db.run(Insert.into(Departments_.class).entry(dept));
    }
}
