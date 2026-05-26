package com.example.capdemo;

import cds.gen.userdepartmentservice.AssignUserToDepartmentContext;
import cds.gen.userdepartmentservice.Departments_;
import cds.gen.userdepartmentservice.GetUsersCountContext;
import cds.gen.userdepartmentservice.Users;
import cds.gen.userdepartmentservice.Users_;
import com.example.capdemo.handlers.UserServiceHandler;
import com.sap.cds.Result;
import com.sap.cds.ql.cqn.CqnInsert;
import com.sap.cds.ql.cqn.CqnSelect;
import com.sap.cds.services.ErrorStatuses;
import com.sap.cds.services.ServiceException;
import com.sap.cds.services.persistence.PersistenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceHandlerTest {

    @Mock
    private PersistenceService db;

    @Mock
    private Result result;

    @InjectMocks
    private UserServiceHandler handler;

    // -------------------------------------------------------------------------
    // validateEuCountry
    // -------------------------------------------------------------------------

    @Test
    void validateEuCountry_WhenCountryIsValidEuMember_ShouldNotThrow() {
        // given
        Users user = TestDataGenerator.createUser();
        user.setAddressCountry("GERMANY");

        // when / then — no exception expected
        handler.validateEuCountry(user);
    }

    @Test
    void validateEuCountry_WhenCountryIsInvalid_ShouldThrowBadRequest() {
        // given
        Users user = TestDataGenerator.createUser();
        user.setAddressCountry("NARNIA");

        // when / then
        assertThatThrownBy(() -> handler.validateEuCountry(user))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorStatus())
                        .isEqualTo(ErrorStatuses.BAD_REQUEST));
    }

    @Test
    void validateEuCountry_WhenCountryIsNull_ShouldNotThrow() {
        // given
        Users user = TestDataGenerator.createUser();
        user.setAddressCountry(null);

        // when / then — null is allowed (address is optional)
        handler.validateEuCountry(user);
    }

    @Test
    void validateEuCountry_WhenCountryIsBlank_ShouldNotThrow() {
        // given
        Users user = TestDataGenerator.createUser();
        user.setAddressCountry("  ");

        // when / then — blank is treated same as null
        handler.validateEuCountry(user);
    }

    // -------------------------------------------------------------------------
    // getUsersCount
    // -------------------------------------------------------------------------

    @Test
    void getUsersCount_WhenCalled_ShouldSetCountAsResult() {
        // given
        GetUsersCountContext ctx = TestDataGenerator.createGetUsersCountContext();
        doReturn(result).when(db).run(any(CqnSelect.class));
        when(result.rowCount()).thenReturn(5L);

        // when
        handler.getUsersCount(ctx);

        // then
        assertThat(ctx.getResult()).isEqualTo(5);
        assertThat(ctx.isCompleted()).isTrue();
    }

    // -------------------------------------------------------------------------
    // assignUserToDepartment
    // -------------------------------------------------------------------------

    @Test
    void assignUserToDepartment_WhenBothExist_ShouldReturnSuccessResult() {
        // given
        String userId = UUID.randomUUID().toString();
        String deptId = UUID.randomUUID().toString();
        AssignUserToDepartmentContext ctx = TestDataGenerator.createAssignContext(userId, deptId);

        // first call: user lookup; second call: department lookup; third call: insert
        doReturn(result).when(db).run(any(CqnSelect.class));
        doReturn(result).when(db).run(any(CqnInsert.class));
        when(result.rowCount()).thenReturn(1L, 1L);

        // when
        handler.assignUserToDepartment(ctx);

        // then
        assertThat(ctx.getResult().getSuccess()).isTrue();
        assertThat(ctx.getResult().getMessage()).contains(userId).contains(deptId);
        assertThat(ctx.isCompleted()).isTrue();
    }

    @Test
    void assignUserToDepartment_WhenUserDoesNotExist_ShouldThrowNotFound() {
        // given
        AssignUserToDepartmentContext ctx = TestDataGenerator.createAssignContext(
                UUID.randomUUID().toString(), UUID.randomUUID().toString());

        doReturn(result).when(db).run(any(CqnSelect.class));
        when(result.rowCount()).thenReturn(0L);

        // when / then
        assertThatThrownBy(() -> handler.assignUserToDepartment(ctx))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorStatus())
                        .isEqualTo(ErrorStatuses.NOT_FOUND));
    }

    @Test
    void assignUserToDepartment_WhenDepartmentDoesNotExist_ShouldThrowNotFound() {
        // given
        AssignUserToDepartmentContext ctx = TestDataGenerator.createAssignContext(
                UUID.randomUUID().toString(), UUID.randomUUID().toString());

        doReturn(result).when(db).run(any(CqnSelect.class));
        // user found (1), department not found (0)
        when(result.rowCount()).thenReturn(1L, 0L);

        // when / then
        assertThatThrownBy(() -> handler.assignUserToDepartment(ctx))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorStatus())
                        .isEqualTo(ErrorStatuses.NOT_FOUND));
    }
}
