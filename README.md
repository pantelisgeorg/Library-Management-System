# Library Management System

A Spring Boot web application for managing a library: books, authors, users and
book borrowings, with an admin dashboard, full CRUD web UI and an
auto-generated REST API backed by PostgreSQL.

## Tech Stack

- Java 17, Spring Boot 3.3
- Spring Web + Thymeleaf (server-rendered UI, Bootstrap 5 via CDN)
- **Spring Security** (form login, BCrypt-hashed passwords, role-based access)
- Spring Data JPA (Hibernate 6)
- **PostgreSQL** (JDBC driver: `org.postgresql:postgresql`)
- Spring Data REST (`/api` endpoints, HAL/JSON)
- Maven

## Prerequisites

- JDK 17
- Maven 3.8+ (a system-wide `mvn` is used; there is no bundled wrapper)
- A running PostgreSQL server

## Configuration

`application.properties` is **gitignored** because it holds your local database
credentials. A committed template,
[`src/main/resources/application.properties.template`](src/main/resources/application.properties.template),
contains all required settings with placeholder values.

After cloning, create your local config from the template:

```bash
cp src/main/resources/application.properties.template \
   src/main/resources/application.properties
```

Then edit the copy and set your connection details:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/library_db
spring.datasource.username=user
spring.datasource.password=password
```

Never commit `application.properties` — only the template is tracked.

## Database Setup

The app connects to a database named `library_db` and stores all tables in a
schema named `libdb` (the entities use `@Table(schema = "libdb")`).

One-time setup (using your own credentials in place of `user`/`password`):

```sql
CREATE DATABASE library_db;
CREATE SCHEMA libdb;           -- connect to library_db first
CREATE USER user WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE library_db TO user;
GRANT ALL ON SCHEMA libdb TO user;
```

With those credentials in place, set them in your local
`src/main/resources/application.properties` (see [Configuration](#configuration)).

On first start, Hibernate creates all tables (`spring.jpa.hibernate.ddl-auto=update`)
and the seeder (`AppRunner`) inserts demo data once: 2 authors, 2 books, 2 users
(admin / john.doe) and a sample borrowing, including the
author-book associations.

## Authentication & Roles

All pages require signing in (Spring Security, form login, BCrypt-hashed passwords).
There are two roles:

| Role   | Can do |
|--------|--------|
| ADMIN  | Everything: view all pages, add/delete books, authors and users, borrow and return books, use the REST API (`/api`) |
| MEMBER | Log in and browse the dashboard, tables and charts **read-only** — no actions, no REST API |

Borrowing is done **by the admin** on behalf of a member (the "Borrow Book" dialog
picks a member and an available book).

Seeded demo accounts:

| Username  | Password  | Role |
|-----------|-----------|------|
| admin     | admin123  | ADMIN |
| john.doe  | user123   | MEMBER |

> **Note:** the seeded passwords are for local development only — change or remove
> them before deploying anywhere public. On startup the app automatically:
> re-hashes legacy plain-text passwords (BCrypt), demotes any old LIBRARIAN
> accounts to MEMBER, and re-creates the demo accounts if they are missing.

## Running

```bash
mvn spring-boot:run
```

Then open:

| Page        | URL                  | Description                                        |
|-------------|----------------------|----------------------------------------------------|
| Dashboard   | http://localhost:8080/            | Stats: total users, books, authors, active borrowings |
| Data Tables | http://localhost:8080/tables       | Full management UI (see below)                     |
| Charts      | http://localhost:8080/charts       | Activity charts (Chart.js)                        |
| REST API    | http://localhost:8080/api          | HAL index of the auto-generated REST API           |

## Web UI Features (`/tables`)

- **Books** — add (modal form: title, ISBN, published date, genre, copies,
  summary) and delete (removes borrow history and author links first)
- **Authors** — add and delete (join rows cleaned up automatically)
- **Users** — add (role picker: Member / Librarian / Admin, duplicate
  username/email protection) and delete (removes borrow history first)
- **Borrowings** — "Borrow Book" dialog (member + available-book dropdowns,
  due date = +14 days, decrements available copies) and "Return" per row
  (sets return date, restores the copy)

All actions show success/error feedback banners.

## REST API (`/api`)

Spring Data REST exposes the repositories automatically:

- `GET /api` — endpoint index
- `GET /api/books`, `/api/authors`, `/api/users`, `/api/borrowings`,
  `/api/bookAuthors` — paginated collections (`?page`, `?size`, `?sort`)
- `GET /api/books/{id}` — single item; `POST`/`PUT`/`PATCH`/`DELETE` for CRUD
- Search endpoints, e.g.
  `GET /api/borrowings/search/findByStatus?status=BORROWED`,
  `GET /api/users/search/findByUsername?username=admin`
- `GET /api/profile` — API metadata

User passwords are write-only (`@JsonProperty(access = WRITE_ONLY)`) and are
never included in API responses.

> **Note:** the REST API requires an **ADMIN** account (HTTP Basic works for
> command-line clients, e.g. `curl -u admin:admin123 http://localhost:8080/api`).
> MEMBER accounts cannot use it. The API is exempted from CSRF protection
> because it is consumed by tools, not by the web forms — all web UI forms
> remain CSRF-protected.

## Project Structure

```
src/main/java/com/library/management/
  LibraryManagementSystemApplication.java   # entry point
  AdminDashboardController.java              # web pages + CRUD endpoints
  RestApiConfig.java                        # serves Data REST under /api
  AppRunner.java                             # seeds demo data once
  Book, Author, User, Borrowing             # JPA entities
  BookAuthor, BookAuthorId                   # join-table entity
  *Repository                               # Spring Data repositories
src/main/resources/
  application.properties.template          # committed config template (copy to application.properties)
  application.properties                   # local config (gitignored, holds DB credentials)
  templates/index.html                      # dashboard
  templates/tables.html                     # management UI (modals, action buttons)
  templates/charts.html                     # Chart.js page
```

## Configuration Notes

- `spring.jpa.hibernate.ddl-auto=update` — Hibernate syncs the schema with the
  entities. Switch to `validate` once the schema is stable.
- All entity tables live in the `libdb` schema.
- SQL logging is verbose (dev-friendly); see `application.properties`.
- CI (`.github/workflows/maven-publish.yml`) builds with JDK 17 and publishes
  to GitHub Packages on release.

## Build & Test

```bash
mvn clean package     # compiles, runs tests, produces target/*.jar
java -jar target/library-management-system-0.0.1-SNAPSHOT.jar
```

## License

This project is licensed under the MIT License — see the
[LICENSE](LICENSE) file for details.

Copyright (c) 2026 GEORGE PANTELIS
