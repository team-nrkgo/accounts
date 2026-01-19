# Accounts Service

## Project Overview
The **Accounts Service** is a Spring Boot application designed to handle user management, authentication, and organization structures. It provides RESTful APIs for signing up, logging in, managing user profiles, and handling organizational memberships and invitations.

## Tech Stack
- **Java**: 17
- **Framework**: Spring Boot 3.x / 4.x
- **Database**: MySQL
- **ORM**: Spring Data JPA (Hibernate)
- **Utilities**: Lombok
- **Security**: Spring Security

## Architecture
The project follows a standard layered architecture:
- **Controller Layer** (`com.nrkgo.accounts.controller`): Handles HTTP requests and responses.
- **Service Layer** (`com.nrkgo.accounts.service`): Contains business logic.
- **Repository Layer** (`com.nrkgo.accounts.repository`): Interacts with the database using JPA.
- **Model/DTO Layer**: Defines entities and data transfer objects.

## Setup & Run Instructions

### Prerequisites
- Java 17+ installed.
- Maven installed.
- MySQL database running.

### Database Setup
1. Create a MySQL database (e.g., `accounts_db`).
2. Run the `src/main/resources/db/accounts.sql` script to initialize the tables.
3. Configure `src/main/resources/application.properties` with your DB credentials:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/accounts_db
   spring.datasource.username=root
   spring.datasource.password=password
   ```

### Running the Application
```bash
./mvnw spring-boot:run
```

## Configuration
- Port: Default `8080` (can be changed in `application.properties`)
- Logging: Configured for `INFO` level generally, `DEBUG` for application package.
