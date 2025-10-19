# Project Structure Documentation

This document explains the architecture and organization of the Booking project.

---

## Table of Contents

- [Project Root](#project-root)
- [Feature Modules](#feature-modules)
  - [availability](#1-availability)
  - [block](#2-block)
  - [booking](#3-booking)
  - [infra](#4-infra)
  - [property](#5-property)
  - [runner](#6-runner)
  - [shared](#7-shared)
- [Summary of Architectural Principles](#summary-of-architectural-principles)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [API Basic Usage](#api-basic-usage)
  - [Blocks API](#blocks-api)
  - [Properties API](#properties-api)
  - [Bookings API](#bookings-api)
- [Quick Reference Table](#quick-reference-table)
- [Schema References](#schema-references)

---

## Project Root

```
src
└── main
    ├── java
    │   └── com
    │       └── hostfully
    │           └── app
```

* **src/main/java**: Contains all Java source code.
* **com.hostfully.app**: The main application package; all business logic, controllers, and infrastructure code live here.

---

## Feature Modules

The project is organized in a **feature-based structure** (vertical slicing), where each domain has its own package.

### 1. `availability`

```
availability
└── service
```

* **Handles**: Availability checks for properties.
* **service**: Contains business logic to compute availability, check if the property is available for a new book or block.

---

### 2. `block`

```
block
├── controller
│   └── dto
├── domain
├── exceptions
└── usecase
```

* **Manages**: Blocking dates or time periods for properties.
* **controller**: REST API endpoints to interact with block functionality.
* **dto**: Data Transfer Objects used in API requests/responses.
* **domain**: Core business models and rules related to blocks.
* **exceptions**: Custom exceptions for block operations (e.g., overlapping blocks).
* **usecase**: Application-specific business logic orchestrating block-related operations.

---

### 3. `booking`

```
booking
├── controller
│   └── dto
├── domain
├── exception
└── usecase
```

* **Handles**: Booking management (creation, cancellation, modification).
* **controller**: API endpoints for booking actions.
* **dto**: Request/response objects for bookings.
* **domain**: Booking entities and business rules.
* **exception**: Booking-specific exceptions.
* **usecase**: Orchestrates booking logic, e.g., book a property or cancel a booking.

---

### 4. `infra`

```
infra
├── entity
├── exception
├── mapper
└── repository
```

* **Contains**: Infrastructure layer, dealing with database, mapping, and persistence.
* **entity**: Database entities (JPA/Hibernate).
* **exception**: Infrastructure-specific exceptions.
* **mapper**: Converts between domain models and database entities.
* **repository**: Interfaces for data access (CRUD operations, queries).

---

### 5. `property`

```
property
├── controller
│   └── dto
├── domain
├── exception
└── usecase
```

* **Manages**: Property-related logic (e.g., property info, amenities).
* **domain**: Core property models.
* **exception**: Property-specific errors.
* **usecase**: Business logic for properties (create, update, fetch).


*!Important:* This extra domain was created solely to simplify interactions with other APIs.
---

### 6. `runner`

Contains the application runner, which creates two properties when the application starts.
The properties can be accessed using the following IDs: *SunnyVilla01* and *CozyNest123.*

---

### 7. `shared`

```
shared
├── config
├── exception
└── util
```

* **Shared resources** across the application.
* **config**: Application configuration classes (e.g., audit config, object mapper).
* **exception**: Global exception handlers.
* **util**: Helper classes and utilities used across modules.

---

## Summary of Architectural Principles

* **Vertical slicing (feature-based modules)** – Each domain (booking, block, property) encapsulates its controllers, domain models, use cases, and exceptions.
* **Separation of concerns** – Each layer (controller, use case, domain, infra) has a single responsibility.
* **Reusability** – Shared utilities and configuration live in a shared package to prevent duplication.
* **Infrastructure decoupling** – Database and persistence details are isolated in the `infra` package.

---

## Running the Application

To run the application, it is required to have **Docker** and **Docker Compose** installed on your machine.

### Build and Start the Application
```bash
docker-compose build booking
docker-compose up booking
```

### Run the Tests
It is also possible to run the tests using Docker Compose:
```bash
docker-compose run tests
```
**Note:** At the end of the test execution, a **test coverage report** will be displayed directly in the terminal.

---

## API Documentation

By default, Swagger UI is available when running the application locally:

```bash
http://localhost:8080/swagger-ui/index.html
```

---
# API Basic Usage

This guide explains how to interact with the Booking API.
---

## Blocks API

### Create a Block

**Endpoint:** `POST /v1/blocks`
**Headers:**

* `Idempotency-Key` (string, UUID, required)

**Request Body:**

```json
{
  "property": "string",
  "reason": "string",
  "startDate": "YYYY-MM-DD",
  "endDate": "YYYY-MM-DD"
}
```

**Response (200 OK):**

```json
{
  "id": "string",
  "propertyId": "string",
  "reason": "string",
  "startDate": "YYYY-MM-DD",
  "endDate": "YYYY-MM-DD"
}
```

---

### Update a Block

**Endpoint:** `PUT /v1/blocks/{id}`
**Path Parameters:**

* `id` (string, required)

**Request Body:** Same as **Create a Block**

**Response (200 OK):** Returns updated block object

---

### Delete a Block

**Endpoint:** `DELETE /v1/blocks/{id}`
**Path Parameters:**

* `id` (string, required)

**Response (200 OK):** Empty

---

## Properties API

### Create a Property

**Endpoint:** `POST /v1/properties`

**Request Body:**

```json
{
  "alias": "string",
  "description": "string"
}
```

**Response (200 OK):**

```json
{
  "id": "string",
  "alias": "string",
  "description": "string"
}
```

---

## Bookings API

### Create a Booking

**Endpoint:** `POST /v1/bookings`
**Headers:**

* `Idempotency-Key` (string, UUID, required)

**Request Body:**

```json
{
  "property": "string",
  "guest": "string",
  "numberGuest": 1,
  "startDate": "YYYY-MM-DD",
  "endDate": "YYYY-MM-DD"
}
```

**Response (200 OK):**

```json
{
  "id": "string",
  "propertyId": "string",
  "startDate": "YYYY-MM-DD",
  "endDate": "YYYY-MM-DD",
  "guestName": "string",
  "numberGuest": 1,
  "status": "string"
}
```

---

### Get a Booking

**Endpoint:** `GET /v1/bookings/{id}`
**Path Parameters:**

* `id` (string, required)

**Response (200 OK):** Returns booking object

---

### Update a Booking

**Endpoint:** `PATCH /v1/bookings/{id}`
**Path Parameters:**

* `id` (string, required)

**Request Body:**

```json
{
  "guest": "string",
  "numberGuest": 1,
  "startDate": "YYYY-MM-DD",
  "endDate": "YYYY-MM-DD"
}
```

**Response (200 OK):** Returns updated booking object

---

### Delete a Booking

**Endpoint:** `DELETE /v1/bookings/{id}`
**Path Parameters:**

* `id` (string, required)

**Response (200 OK):** Empty

---

### Rebook a Booking

**Endpoint:** `POST /v1/bookings/{id}/rebook`
**Headers:**

* `Idempotency-Key` (string, UUID, required)
  **Path Parameters:**
* `id` (string, required)

**Request Body:**

```json
{
  "startDate": "YYYY-MM-DD",
  "endDate": "YYYY-MM-DD"
}
```

**Response (200 OK):** Returns updated booking object

---

### Cancel a Booking

**Endpoint:** `POST /v1/bookings/{id}/cancel`
**Headers:**

* `Idempotency-Key` (string, UUID, required)
  **Path Parameters:**
* `id` (string, required)

**Response (200 OK):** Returns booking object with updated status

---

## Quick Reference Table

| Endpoint                 | Method | Headers         | Path Params | Request Body         | Response |
| ------------------------ | ------ | --------------- | ----------- | -------------------- | -------- |
| /v1/blocks               | POST   | Idempotency-Key | –           | BlockRequest         | Block    |
| /v1/blocks/{id}          | PUT    | –               | id          | BlockRequest         | Block    |
| /v1/blocks/{id}          | DELETE | –               | id          | –                    | 200 OK   |
| /v1/properties           | POST   | –               | –           | PropertyRequest      | Property |
| /v1/bookings             | POST   | Idempotency-Key | –           | BookingRequest       | Booking  |
| /v1/bookings/{id}        | GET    | –               | id          | –                    | Booking  |
| /v1/bookings/{id}        | PATCH  | –               | id          | UpdateBookingRequest | Booking  |
| /v1/bookings/{id}        | DELETE | –               | id          | –                    | 200 OK   |
| /v1/bookings/{id}/rebook | POST   | Idempotency-Key | id          | RebookBookingRequest | Booking  |
| /v1/bookings/{id}/cancel | POST   | Idempotency-Key | id          | –                    | Booking  |

---

## Schema References

### BlockRequest

```json
{
  "alias": "string",
  "property": "string",
  "reason": "string",
  "startDate": "YYYY-MM-DD",
  "endDate": "YYYY-MM-DD"
}
```

### Block

```json
{
  "id": "string",
  "propertyId": "string",
  "reason": "string",
  "startDate": "YYYY-MM-DD",
  "endDate": "YYYY-MM-DD"
}
```

### PropertyRequest

```json
{
  "alias": "string",
  "description": "string"
}
```

### Property

```json
{
  "id": "string",
  "alias": "string",
  "description": "string"
}
```

### BookingRequest

```json
{
  "property": "string",
  "guest": "string",
  "numberGuest": 1,
  "startDate": "YYYY-MM-DD",
  "endDate": "YYYY-MM-DD"
}
```

### Booking

```json
{
  "id": "string",
  "propertyId": "string",
  "startDate": "YYYY-MM-DD",
  "endDate": "YYYY-MM-DD",
  "guestName": "string",
  "numberGuest": 1,
  "status": "string"
}
```

### RebookBookingRequest

```json
{
  "startDate": "YYYY-MM-DD",
  "endDate": "YYYY-MM-DD"
}
```

### UpdateBookingRequest

```json
{
  "guest": "string",
  "numberGuest": 1,
  "startDate": "YYYY-MM-DD",
  "endDate": "YYYY-MM-DD"
}
```

