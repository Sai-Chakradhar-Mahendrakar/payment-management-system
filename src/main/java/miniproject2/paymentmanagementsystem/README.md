# Payment Management System

A secure Spring Boot REST API backend for managing payment flows with JWT authentication, role-based access control, and comprehensive traceability.

# Problem Statement

## Fintech Payment Management System - Spring Boot REST API

A secure internal backend system to manage payment flows with traceability, reporting, and role-based access.

---

## **Core Features**

### 1. User Management

**Entity:** `User`

| Field   | Type   | Description                                |
|---------|--------|--------------------------------------------|
| id      | Long   | Unique identifier for the user             |
| name    | String | Full name of the user                      |
| email   | String | Unique email address                       |
| role    | Enum   | Role of the user (`ADMIN`, `FINANCE_MANAGER`, `VIEWER`) |

**Endpoints:**

| Method | Endpoint  | Description      |
|--------|-----------|------------------|
| POST   | `/users`  | Create a new user |
| GET    | `/users`  | List all users    |

---

### 2. Payment Management

**Entity:** `Payment`

| Field       | Type     | Description                                    |
|-------------|----------|------------------------------------------------|
| id          | Long     | Unique payment ID                              |
| amount      | Decimal  | Payment amount                                 |
| paymentType | Enum     | Type of payment (`INCOMING`, `OUTGOING`)       |
| category    | Enum     | Payment category (`SALARY`, `VENDOR`, `INVOICE`) |
| status      | Enum     | Payment status (`PENDING`, `PROCESSING`, `COMPLETED`) |
| date        | DateTime | Date of payment creation/transaction          |
| createdBy   | Long     | ID of the user who created the payment         |

**Endpoints:**

| Method | Endpoint           | Description                  |
|--------|--------------------|------------------------------|
| POST   | `/payments`        | Create a payment              |
| GET    | `/payments`        | List all payments             |
| GET    | `/payments/{id}`   | Get payment by ID              |
| PUT    | `/payments/{id}`   | Update payment details         |
| DELETE | `/payments/{id}`   | Delete a payment               |

---


## Features

### Core Functionality
- **User Management** with role-based access control (ADMIN, FINANCE_MANAGER, VIEWER)
- **Payment Management** with full CRUD operations
- **JWT Authentication** for secure API access
- **Role-based Authorization** for different user types
- **Payment Status Tracking** (PENDING, PROCESSING, COMPLETED)
- **Payment Categorization** (SALARY, VENDOR, INVOICE)
- **Audit Trail** with creation and update timestamps

### Security Features
- JWT-based authentication
- Role-based access control
- Password encryption using BCrypt
- Method-level security annotations

## Technology Stack

- **Framework**: Spring Boot 3.5.4
- **Security**: Spring Security with JWT
- **Database**: PostgreSQL with JPA/Hibernate
- **Validation**: Bean Validation (JSR-303)
- **Documentation**: Built-in exception handling

## Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 12+

### Database Setup
1. Create a PostgreSQL database named `payment_management`
2. Update database credentials in `application.properties`

### Running the Application
```bash
# Clone the repository
git clone <repository-url>
cd payment-management-system

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Default Users

The system creates default users on startup:

| Email | Password | Role |
|-------|----------|------|
| admin@payment.com | admin123 | ADMIN |
| finance@payment.com | finance123 | FINANCE_MANAGER |
| viewer@payment.com | viewer123 | VIEWER |

## API Documentation

### Authentication Endpoints

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
    "email": "admin@payment.com",
    "password": "admin123"
}
```

**Response:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "userId": 1,
    "name": "System Admin",
    "email": "admin@payment.com",
    "role": "ADMIN"
}
```

### User Management Endpoints (ADMIN only)

#### Create User
```http
POST /api/users
Authorization: Bearer <token>
Content-Type: application/json

{
    "name": "John Doe",
    "email": "john@payment.com",
    "password": "password123",
    "role": "FINANCE_MANAGER"
}
```

#### Get All Users
```http
GET /api/users
Authorization: Bearer <token>
```

#### Get User by ID
```http
GET /api/users/{id}
Authorization: Bearer <token>
```

### Payment Management Endpoints

#### Create Payment (ADMIN, FINANCE_MANAGER)
```http
POST /api/payments
Authorization: Bearer <token>
Content-Type: application/json

{
    "amount": 5000.00,
    "paymentType": "OUTGOING",
    "category": "SALARY",
    "status": "PENDING",
    "date": "2025-08-12T10:00:00",
    "description": "Employee salary payment"
}
```

#### Get All Payments (All roles)
```http
GET /api/payments
Authorization: Bearer <token>
```

#### Get Payment by ID (All roles)
```http
GET /api/payments/{id}
Authorization: Bearer <token>
```

#### Update Payment (ADMIN, FINANCE_MANAGER)
```http
PUT /api/payments/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
    "amount": 5500.00,
    "status": "COMPLETED",
    "description": "Updated salary payment"
}
```

#### Delete Payment (ADMIN only)
```http
DELETE /api/payments/{id}
Authorization: Bearer <token>
```

#### Get My Payments (All roles)
```http
GET /api/payments/my-payments
Authorization: Bearer <token>
```

## Role-Based Access Control

### ADMIN
- Create users
- View all users
- Create payments
- View all payments
- Update payments
- Delete payments

### FINANCE_MANAGER
- Create payments
- View all payments
- Update own payments
- View own payments

### VIEWER
- View all payments
- View own payments

## Testing with Postman/cURL

### 1. Login to get JWT token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@payment.com",
    "password": "admin123"
  }'
```

### 2. Create a payment
```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000.00,
    "paymentType": "OUTGOING",
    "category": "VENDOR",
    "status": "PENDING",
    "date": "2025-08-12T10:00:00",
    "description": "Vendor payment"
  }'
```

### 3. Get all payments
```bash
curl -X GET http://localhost:8080/api/payments \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Error Handling

The API provides comprehensive error handling with detailed error messages:

- **400 Bad Request**: Validation errors
- **401 Unauthorized**: Invalid credentials
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server errors

## Configuration

### JWT Configuration
- Secret key: Configurable in `application.properties`
- Token expiration: 24 hours (configurable)

### Database Configuration
- Connection pool configuration
- Hibernate DDL auto-update
- SQL logging for development

## Security Considerations

1. **Password Encryption**: All passwords are encrypted using BCrypt
2. **JWT Security**: Tokens are signed and validated
3. **Role-based Access**: Method-level security annotations
4. **Input Validation**: Comprehensive validation on all endpoints
5. **CORS**: Configured for cross-origin requests
