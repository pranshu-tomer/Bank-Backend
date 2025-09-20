<h1 align="center">
  Voltrex Bank — Backend (Spring Boot)
</h1>

VoltRex Bank is a **fun full-stack style backend project** built with Spring Boot that brings core banking features to life. 🚀  
From **signing up and getting accounts approved** to **creating multiple account types, handling transfers.**, this project packs in the essentials of a modern banking system.  
It’s not just CRUD — it includes **real transaction rules, monthly interest/fees, and JWT security** to keep things realistic. Perfect for exploring how enterprise-style banking apps work under the hood! 💳🏦

---

<div align="center">

<!-- **GitHub Badges** -->
[![GitHub last commit](https://img.shields.io/github/last-commit/pranshu-tomer/Prescripto)](https://github.com/pranshu-tomer/Prescripto/commits/main)
[![GitHub contributors](https://img.shields.io/github/contributors/pranshu-tomer/Prescripto)](https://github.com/pranshu-tomer/Prescripto/graphs/contributors)
[![GitHub stars](https://img.shields.io/github/stars/pranshu-tomer/Prescripto)](https://github.com/pranshu-tomer/Prescripto/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/pranshu-tomer/Prescripto)](https://github.com/pranshu-tomer/Prescripto/network/members)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](http://makeapullrequest.com)

<!-- **Technology Icons** using Shields.io and Devicons -->
<div>
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java"/>
  <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" alt="Spring Security"/>
  <img src="https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white" alt="Hibernate"/>
  <img src="https://img.shields.io/badge/H2%20Database-005C84?style=for-the-badge&logo=h2&logoColor=white" alt="H2 Database"/>
  <img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL"/>
  <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" alt="JWT"/>
  <img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven"/>
  <img src="https://img.shields.io/badge/Lombok-FF5733?style=for-the-badge&logoColor=white" alt="Lombok"/>
  <img src="https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white" alt="JUnit 5"/>
  <img src="https://img.shields.io/badge/Apache%20PDFBox-231F20?style=for-the-badge&logo=adobeacrobatreader&logoColor=red" alt="PDFBox"/>
</div>
</div>

---

## Live Preview

Try out **Voltrex Bank** using the links and demo credentials below:

### 🔗 Application Links

- **Live Portal:** [https://voltrex-bank.netlify.app/](https://voltrex-bank.netlify.app/)
> ⚠️ **Note:** This project is hosted on a free cloud service.  
> The server may take a few seconds to "wake up" on the first request, so you might experience some initial latency.

### 🧑‍💼 Demo credentials

- **CRN:** CRN0000002
- **Password:** pranshu1234

---

## Overview
VoltRex Bank is a micro/bounded-domain-style backend implementing:

- User registration and admin approval workflow
- Multiple account types (SAVINGS, SALARY, CURRENT) with fixed per-type rules (interest, monthly fee, min balance, daily/monthly limits)
- Account creation rules (1 of each type per user)
- Transfers (by account number and by receiver identifier (CRN/email)), ACID-safe
- Transaction recording with fromAccountBalanceAfter / toAccountBalanceAfter snapshots
- Monthly processing (interest, fees, penalties) — designed to be run as scheduled job or Spring Batch
- REST API with JWT authentication
- This README documents how to run and how the important pieces are implemented.

---

## Features

✔ **User Registration & Admin Approval Workflow**  
✔ **Multi-role Authentication** (Admin & User)  
✔ **Fixed Account Types** (Savings, Salary, Current) with predefined rules   
✔ **Account Creation Rules** (only one of each account type per user)  
✔ **Secure JWT-based Authentication & Authorization**  
✔ **Funds Transfer** (by account number, CRN, or email with name verification)  
✔ **Transaction History with Snapshots** (debit, credit, internal)  
✔ **Monthly Processing** (interest credit, fee deduction, minimum balance penalty)  
✔ **Developer Utilities** (test top-up endpoint for accounts)  
✔ **Responsive Error Handling** (consistent JSON for 401/403/400 responses)  

--- 

## Tech Stack

Here’s a quick look at the technologies used in this project.

| Layer        | Technology                                | Icon |
|--------------|-------------------------------------------|------|
| Language     | Java 17+                                   | <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" width="80"/> |
| Framework    | Spring Boot, Spring Security               | <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" width="120"/> |
| ORM / JPA    | Hibernate ORM                              | <img src="https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white" width="100"/> |
| Database     | H2 (Dev/Test), PostgreSQL / MySQL (Prod)   | <img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" width="110"/> <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" width="80"/> |
| Auth         | JWT (JSON Web Tokens), BCrypt Passwords    | <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" width="70"/> |
| Build Tool   | Maven                                      | <img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" width="90"/> |
| Utilities    | Lombok, Apache PDFBox                      | <img src="https://img.shields.io/badge/Lombok-FF5733?style=for-the-badge&logoColor=white" width="90"/> <img src="https://img.shields.io/badge/PDFBox-231F20?style=for-the-badge&logo=adobeacrobatreader&logoColor=red" width="90"/> |
| Testing      | JUnit 5, Spring Boot Test                  | <img src="https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white" width="90"/> |

---

## Key concepts & domain model

Top-level domain entities (package `com.voltrex.bank.entities`):

- `User` — customers, with `status` enum (PENDING, APPROVED, REJECTED) for admin approval. Each user has list of `Account`.
- `Account` — types defined by `AccountType` (SAVINGS, SALARY, CURRENT). Key fields:
  - `accountNumber`, `type`, `balance`, `openedAt`, `primaryAccount`, `monthlyFee`, `minimumBalance`, `interestRate`, `maxDailyWithdrawal`, `maxDailyDeposit`
- `Transaction` — records transfers/fees/interest. Important to keep:
  - `fromAccount`, `toAccount`, `amount`, `type`, `executedAt`, `fromAccountBalanceAfter`, `toAccountBalanceAfter`
- `JobRun` (or `Job_Run`) — idempotency for monthly processing jobs (job name + period unique)

**Design notes:** always persist snapshot balances within the same `@Transactional` method that updates account balances.

---

## ERD (Entity Relationship Diagram)

---

## Getting started (local)

### Prerequisites

- Java 17+ installed
- Maven 3.8+
- (Optional) Docker if you want to run DB in container
- (Optional) Node.js & npm if you run a frontend

### Clone and build

```

git clone https://github.com/pranshu-tomer/Bank-Backend.git
cd voltrex-bank-backend
mvn -DskipTests clean package

```

### Run (development, using H2 embedded)

By default the app can run with H2 in-memory DB for local dev. Example:
```

mvn spring-boot:run
# or run the fat jar
java -jar target/bank-0.0.1-SNAPSHOT.jar

```

The app listens on `http://localhost:8080` by default.

<hr/>

## Configuration / environment variables

Copy `application.properties` set environment-specific variables.

### Example `application.properties` (dev):

```

spring.application.name=bank

# database connection
spring.datasource.url=jdbc:h2:mem:bankdb;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# email service
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=...
spring.mail.password=...
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# JWT Key
jwt.secretKey=...

frontend.url=http://localhost:5173/

```

> **Important:** Never commit secrets (JWT secret, SMTP password) to VCS. Use environment variables for production.

---

## APIs overview (important endpoints)

> Base path: `/api`

### Authentication

- `POST /api/auth/register` — register user (creates user with `status = PENDING`). Request includes `firstName, lastName, email, phone, address, dob, age, gender.`
- `POST /api/auth/login` — login with `crn` and `password`. Returns `{ success: true, token: <jwt> }` on success.
  - **Note:** on authentication failure, returns 401 with JSON `{ success: false, error: "Invalid credentials" }`

### User/Admin

- `POST /api/admin/users/{userId}/approve` — Admin approves user. Should:
  - generate CRN and temporary password
  - create default SAVINGS account (primary) for user
  - set `user.status = APPROVED`
  - send account approved mail (if mail service configured)

### Accounts

- `GET /api/accounts` — return all accounts for authenticated user, with:
  - accountNumber / cardNumber, type, balance, interestRate, openedAt, minimumBalance, monthlyFee, thisMonthIn, thisMonthOut, isPrimary
- `POST /api/accounts/create` — create new account for current user (body: `{ "type": "SALARY" }`) — enforces rules:
  - user cannot create SAVINGS if they already have one
  - user cannot exceed 3 account types total
  - creates credit card if requested and not exists
- `POST /api/accounts/topup/{accountNumber}` (dev) — test-only topup route to add balance.

### Transfers

- `POST /api/transfer/byAccount` — transfer by account number (validated: owner, name match, limits, sufficient balance)
- `POST /api/transfer/byReceiver` — transfer by receiver identifier (CRN or email + receiverName + amount + type)

Request DTOs include `amount`, `description`, `type` (TransactionType), and are validated with Jakarta Validation.

### Transactions

- `GET /api/transactions?from=<ISO_DATETIME>&to=<ISO_DATETIME>&page=0&size=25&export=true|false`
  - It returns paged JSON with `TransactionResponse` which includes `senderName`, `receiverName`, `direction` (DEBIT / CREDIT / INTERNAL) and `fromAccountBalanceAfter` / `toAccountBalanceAfter`.

---

## Security & JWT notes

- The project uses JWT tokens issued at login. Token contains `userId` claim used by the `JwtFilter` to load a User and set the `SecurityContext`.
- Recommended `PasswordEncoder: BCryptPasswordEncoder` as a bean.
- **Do not** return sensitive fields (e.g., passwordHash) in API responses.

**Frontend storage:**
  - If storing token in localStorage, it **won't auto-expire**.
  - Alternative: store token in HttpOnly cookie (more secure against XSS).
    
- Use a custom `AuthenticationEntryPoint` to unify `401` responses for filter-level failures (so JSON body is consistent).

---

## Monthly processing / scheduled jobs

Monthly activities (interest credit, monthly fee, minimum-balance penalties) should be:
  - executed once per period and idempotent (guard via job_run table)
  - atomic per-account (use per-account transactions/chunks)
  - optionally run via Spring Batch for restartable, chunked processing
  - protected from concurrent runs by ShedLock (DB/Redis) if running multiple instances
Main implementation points:
  - compute interest (policy: closing balance or average daily balance — if average, keep daily snapshots)
  - apply fees & penalties as transactions (transactions must include fromAccountBalanceAfter/toAccountBalanceAfter)
  - store JobRun record for audit and idempotency

---

<p align="center"> 
<b>Made with ❤ by Pranshu Tomer and contributors.</b>
</p>
