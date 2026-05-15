namespace com.example.demo;

// EU member states (27 members as of 2024).
// CAP automatically rejects values outside this enum via @assert.range – no handler needed.
// Spring Boot equivalent: entity/EuCountry.java (Java enum) stored via @Enumerated(EnumType.STRING)
type EuCountry : String enum {
  AUSTRIA;
  BELGIUM;
  BULGARIA;
  CROATIA;
  CYPRUS;
  CZECHIA;
  DENMARK;
  ESTONIA;
  FINLAND;
  FRANCE;
  GERMANY;
  GREECE;
  HUNGARY;
  IRELAND;
  ITALY;
  LATVIA;
  LITHUANIA;
  LUXEMBOURG;
  MALTA;
  NETHERLANDS;
  POLAND;
  PORTUGAL;
  ROMANIA;
  SLOVAKIA;
  SLOVENIA;
  SPAIN;
  SWEDEN;
}

// CAP equivalent of JPA @Embeddable.
// Stored as flat columns (no join table), but serialised as a nested object in OData JSON.
// Spring Boot equivalent: entity/Address.java with @Embeddable
type Address {
  country : EuCountry;
  city    : String(100);
}

// Spring Boot equivalent: entity/User.java with @Entity @Table(name="users")
@assert.unique: { email: [email] }
entity Users {
  key ID          : UUID;
      // @mandatory rejects null and blank strings at the service layer (before DB).
      // Spring Boot equivalent: @NotBlank on DTO fields + @Valid in controller
      firstName   : String(100) not null @mandatory;
      lastName    : String(100) not null @mandatory;
      // @mandatory enforces non-blank; @assert.format validates the email pattern.
      // Spring Boot equivalent: @NotBlank @Email on DTO fields
      email       : String(255) @mandatory @assert.format: '^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$';
      address     : Address;
      // Navigation to the explicit join entity (see note on UserDepartments below)
      departments : Association to many UserDepartments
                      on departments.user = $self;
}

// Spring Boot equivalent: entity/Department.java with @Entity
entity Departments {
  key ID      : UUID;
      // Spring Boot equivalent: @NotBlank on CreateDepartmentRequest.name
      name    : String(100) not null @mandatory;
      members : Association to many UserDepartments
                  on members.department = $self;
}

// CAP has no @JoinTable equivalent. Many-to-many requires an explicit join entity.
// Spring Boot equivalent: @JoinTable(name="user_departments") on User.departments
entity UserDepartments {
  key user       : Association to Users;
  key department : Association to Departments;
}
