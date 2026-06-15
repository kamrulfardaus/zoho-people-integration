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
# Oracle Database Objects and PL/SQL Procedures

## Tables

### 1. ZOHO_LEAVE_RECORD

Stores leave/absence records fetched from Zoho Leave Tracker.

```sql
CREATE TABLE XFACTORPRO.ZOHO_LEAVE_RECORD (
    ZOHO_ID          NUMBER PRIMARY KEY,
    EMPLOYEE_ID      VARCHAR2(50),
    EMPLOYEE_ZOHO_ID NUMBER,
    EMPLOYEE_NAME    VARCHAR2(200),
    LEAVE_TYPE_ID    NUMBER,
    LEAVE_TYPE       VARCHAR2(100),
    FROM_DATE        DATE,
    TO_DATE          DATE,
    LEAVE_DATE       DATE,
    LEAVE_COUNT      NUMBER,
    APPROVAL_STATUS  VARCHAR2(50),
    LEAVE_TYPE_CODE  VARCHAR2(50),
    DATE_OF_REQUEST  DATE,
    RAW_JSON         CLOB,
    CREATED_AT       DATE DEFAULT SYSDATE,
    UPDATED_AT       DATE
);
```

---

### 2. ZOHO_LEAVE_TYPE_DETAILS

Stores employee-wise leave type, entitlement, availed count, and balance information.

```sql
CREATE TABLE XFACTORPRO.ZOHO_LEAVE_TYPE_DETAILS
(
    USER_ID                VARCHAR2(100),
    LEAVE_TYPE_ID          VARCHAR2(100),
    LEAVE_TYPE_NAME        VARCHAR2(200),
    LEAVE_CODE             VARCHAR2(50),
    LEAVE_TYPE             VARCHAR2(100),
    TYPE_OF_LEAVE          NUMBER,
    UNIT                   VARCHAR2(20),
    COLOR                  VARCHAR2(50),
    PERMITTED_COUNT        NUMBER,
    AVAILED_COUNT          NUMBER,
    BALANCE_COUNT          NUMBER,
    IS_HALF_DAY_ENABLED    VARCHAR2(5),
    IS_QUARTER_DAY_ENABLED VARCHAR2(5),
    IS_HOUR_ENABLED        VARCHAR2(5),
    DO_NOT_DISPLAY_BALANCE VARCHAR2(5),
    SHOW_FILE_UPLOAD_AFTER NUMBER,
    VERSION_NO             NUMBER,
    RAW_JSON               CLOB,
    CREATED_AT             DATE DEFAULT SYSDATE,
    UPDATED_AT             DATE,
    CONSTRAINT ZOHO_LEAVE_TYPE_DTL_UK UNIQUE (USER_ID, LEAVE_TYPE_ID)
);
```

---

## PL/SQL Procedures

### 1. Insert Leave Request

Calls Spring Boot API:

```text
POST http://localhost:8085/zoho/insert-leave
```

```sql
CREATE OR REPLACE PROCEDURE XFACTORPRO.CALL_ZOHO_INSERT_LEAVE (
    p_employee_id   IN VARCHAR2,
    p_leave_type_id IN VARCHAR2,
    p_from_date     IN VARCHAR2,
    p_to_date       IN VARCHAR2,
    p_leave_date    IN VARCHAR2,
    p_leave_count   IN NUMBER,
    p_session       IN NUMBER
)
IS
    l_url       VARCHAR2(1000) := 'http://localhost:8085/zoho/insert-leave';
    l_body      CLOB;
    l_response  CLOB;
BEGIN
    l_body :=
        '{' ||
        '"employeeId":"'   || p_employee_id   || '",' ||
        '"leaveTypeId":"'  || p_leave_type_id || '",' ||
        '"fromDate":"'     || p_from_date     || '",' ||
        '"toDate":"'       || p_to_date       || '",' ||
        '"leaveDate":"'    || p_leave_date    || '",' ||
        '"leaveCount":'    || TO_CHAR(p_leave_count, 'FM9999990D999', 'NLS_NUMERIC_CHARACTERS=.,') || ',' ||
        '"session":'       || p_session ||
        '}';

    APEX_WEB_SERVICE.G_REQUEST_HEADERS.DELETE;

    APEX_WEB_SERVICE.G_REQUEST_HEADERS(1).NAME  := 'Content-Type';
    APEX_WEB_SERVICE.G_REQUEST_HEADERS(1).VALUE := 'application/json';

    APEX_WEB_SERVICE.G_REQUEST_HEADERS(2).NAME  := 'Accept';
    APEX_WEB_SERVICE.G_REQUEST_HEADERS(2).VALUE := 'application/json';

    l_response := APEX_WEB_SERVICE.MAKE_REST_REQUEST(
        p_url         => l_url,
        p_http_method => 'POST',
        p_body        => l_body
    );

    DBMS_OUTPUT.PUT_LINE('Response: ' || DBMS_LOB.SUBSTR(l_response, 4000, 1));
END;
/
```

Example:

```sql
BEGIN
    XFACTORPRO.CALL_ZOHO_INSERT_LEAVE(
        p_employee_id   => '790982000017549734',
        p_leave_type_id => '790982000000457803',
        p_from_date     => '2026-01-20',
        p_to_date       => '2026-01-21',
        p_leave_date    => '2026-01-20',
        p_leave_count   => 0.5,
        p_session       => 2
    );
END;
/
```

---

### 2. Sync Leave Type Details

Calls Spring Boot API:

```text
GET http://localhost:8085/zoho/leave-types?userId=<USER_ID>
```

The response is parsed using `APEX_JSON` and inserted/updated into `ZOHO_LEAVE_TYPE_DETAILS`.

Procedure name:

```sql
XFACTORPRO.SYNC_ZOHO_LEAVE_TYPES
```

Example:

```sql
BEGIN
    XFACTORPRO.SYNC_ZOHO_LEAVE_TYPES(
        p_user_id => '790982000017549734'
    );
END;
/
```

---

### 3. Sync Leave Records

Calls Spring Boot API:

```text
GET http://localhost:8085/zoho/leave-records/raw?from=<FROM_DATE>&to=<TO_DATE>
```

The response is parsed and inserted/updated into `ZOHO_LEAVE_RECORD`.

Procedure name:

```sql
XFACTORPRO.SYNC_ZOHO_LEAVE_RECORDS
```

Example:

```sql
BEGIN
    XFACTORPRO.SYNC_ZOHO_LEAVE_RECORDS(
        p_from_date => '2026-05-01',
        p_to_date   => '2026-12-31'
    );
END;
/
```

---

## Notes

* If Oracle DB and Spring Boot API are not on the same server, replace `localhost` with the actual Java API server IP or domain.
* `APEX_WEB_SERVICE` is used to call the Spring Boot API from Oracle.
* `APEX_JSON` is used to parse Zoho JSON responses.
* `MERGE` statements are used for insert/update logic.
* OAuth token handling is managed inside the Spring Boot API.
* Oracle does not call Zoho directly; Oracle calls the Java API, and Java communicates with Zoho.

## Author

Kamrul Fardaus
