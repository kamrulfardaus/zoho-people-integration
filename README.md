# Zoho People Integration Service

## Overview

Spring Boot based integration service between Oracle Database/APEX and Zoho People.

The service exposes REST APIs which can be consumed from Oracle PL/SQL using APEX_WEB_SERVICE.

## Features

### Employee Management

* Get Employee Records
* Sync Employee Records
* Insert Employee
* Update Employee

### Leave Management

* Insert Leave
* Get Leave Records
* Get Leave Type Details

### Authentication

* OAuth Token Refresh
* In-Memory Access Token Cache
* Separate Authentication Support for Leave APIs

### Oracle Integration

* APEX_WEB_SERVICE Integration
* JSON Parsing
* MERGE Based Upsert Logic

---

## Technology Stack

### Backend

* Java 17
* Spring Boot
* Maven
* Jackson

### Database

* Oracle 19c / 21c
* Oracle APEX

### External System

* Zoho People

---

## Project Structure

src/main/java

controller/

* ZohoController

service/

* ZohoService
* ZohoAuthService
* ZohoLeaveAuthService

dto/

* EmployeeDto
* ZohoInsertRequest
* ZohoInsertResponse
* ZohoLeaveInsertRequest
* ZohoLeaveInsertResponse

repository/

* EmployeeRepository

---

## Available APIs

### Employee

GET /zoho/raw

GET /zoho/sync-all

POST /zoho/insert-employee

POST /zoho/update-employee

### Leave

POST /zoho/insert-leave

GET /zoho/leave-records/raw

GET /zoho/leave-types

---

## Deployment

Build:

mvn clean package -DskipTests

Run:

java -jar getemp-0.0.1-SNAPSHOT.jar --spring.config.location=file:C:/apps/zoho-sync/config/application.properties

---

## Oracle Integration

Oracle procedures use:

APEX_WEB_SERVICE.MAKE_REST_REQUEST

to communicate with this service.

---

## Author

Tawfiq Sattar
ERA InfoTech Ltd.
