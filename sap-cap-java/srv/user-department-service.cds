using com.example.demo from '../db/data-model';

// All endpoints require at least an authenticated user.
// Spring Boot equivalent: .anyRequest().authenticated() in SecurityConfiguration
@requires: 'authenticated-user'
service UserDepartmentService {

  // @restrict replaces @PreAuthorize on every controller method.
  // admin → full CRUD, user → read-only
  @restrict: [
    { grant: '*',    to: 'admin' },
    { grant: 'READ', to: 'user'  }
  ]
  entity Users as projection on demo.Users;

  @restrict: [
    { grant: '*',    to: 'admin' },
    { grant: 'READ', to: 'user'  }
  ]
  entity Departments as projection on demo.Departments;

  // Access controlled by the service-level @requires above
  entity UserDepartments as projection on demo.UserDepartments;

  // OData Function – read-only, no side effects, called via GET.
  // Spring Boot equivalent: GET /api/users/count
  @requires: ['admin', 'user']
  function getUsersCount() returns Integer;

  // OData Action – can mutate state, called via POST.
  // Spring Boot equivalent: POST /api/users/{id}/assign-department
  @requires: 'admin'
  action assignUserToDepartment(userID: UUID, departmentID: UUID)
    returns {
      success   : Boolean;
      message   : String;
    };
}
