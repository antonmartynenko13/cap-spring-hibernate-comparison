# Backlog

Items to add to the comparison demo. Feel free to pick one up, implement it in both projects, and update the side-by-side table in the root README.

New ideas are very welcome — open a PR or just bring it up in discussion.

---

## Pending

- **Error envelope** – compare how each stack serialises validation and business errors to the client. Spring Boot: `@ControllerAdvice` / `ProblemDetail`; CAP: OData error format (`error.code`, `error.message`, `error.details`).

- **Built-in OData query capabilities** – demonstrate `$filter`, `$expand`, `$select`, `$count` on real data. Show what Spring Boot needs to replicate each feature vs. zero-code CAP support. Useful for showing how much "free" querying OData provides.

- **N+1 problem** – show how the N+1 query issue manifests when loading Users with their Departments, and how each stack addresses it: `@EntityGraph` / `JOIN FETCH` in Spring Boot vs. `$expand` with CAP's managed associations.

- **Schema migration: Liquibase vs CAP HDI container** – compare database schema lifecycle management. Spring Boot: Liquibase changelogs with versioned migrations; CAP: HDI (HANA Deployment Infrastructure) container on SAP BTP, and the `cds deploy` / `cds-services` approach for local H2 dev vs. production HANA.