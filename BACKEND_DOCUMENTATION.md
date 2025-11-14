# Backend Architecture Documentation

## Table of Contents
1. [Overview](#overview)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Core Architecture](#core-architecture)
5. [API Endpoints](#api-endpoints)
6. [Data Models & DTOs](#data-models--dtos)
7. [Service Layer](#service-layer)
8. [Process Workflows](#process-workflows)
9. [Configuration](#configuration)
10. [Error Handling](#error-handling)
11. [Database Schema](#database-schema)
12. [Deployment Flow](#deployment-flow)
13. [Integration Points](#integration-points)

---

## Overview

This backend application is a **Spring Boot-based workflow management system** built on the **Flowable BPM Engine**. It provides comprehensive APIs for managing business process workflows, particularly focused on maker-checker approval patterns and retention offer processes.

### Key Characteristics
- **Framework**: Spring Boot 3.2.0
- **BPM Engine**: Flowable 7.2.0
- **Database**: H2 (in-memory, suitable for development)
- **API Documentation**: OpenAPI 3.0 (Swagger)
- **Architecture**: Layered (Controller → Service → Repository)
- **Java Version**: 17

---

## Technology Stack

### Core Dependencies

```xml
<!-- Spring Boot Starters -->
- spring-boot-starter-web (REST API)
- spring-boot-starter-data-jpa (Database access)
- spring-boot-starter-cache (Caching support)

<!-- Flowable BPM -->
- flowable-spring-boot-starter-process (v7.2.0)
- flowable-spring-boot-starter (v7.2.0)

<!-- Database -->
- H2 Database (in-memory)

<!-- API Documentation -->
- springdoc-openapi-starter-webmvc-ui (v2.3.0)

<!-- Utilities -->
- Lombok (Reduces boilerplate code)
```

### Build Tool
- **Maven** (pom.xml)
- Java 17 target
- Spring Boot Maven Plugin for packaging

---

## Project Structure

```
backend/
├── src/main/java/com/example/backend/
│   ├── BackendApplication.java          # Main entry point
│   ├── config/                          # Configuration classes
│   │   ├── CacheConfig.java            # Cache configuration
│   │   └── CorsConfig.java             # CORS configuration
│   ├── controller/                      # REST API controllers
│   │   ├── FlowableDeploymentController.java
│   │   ├── FlowableDiagramController.java
│   │   ├── FlowableEngineInfoController.java
│   │   ├── FlowableHistoryController.java
│   │   ├── FlowableModelController.java
│   │   ├── FlowableProcessDefinitionController.java
│   │   ├── FlowableRuntimeController.java
│   │   ├── FlowableTaskController.java
│   │   ├── ProcessController.java
│   │   └── RetentionOfferController.java
│   ├── delegates/                       # Flowable service task delegates
│   │   ├── DbUpdateDelegate.java
│   │   ├── NotificationDelegate.java
│   │   └── ValidationDelegate.java
│   ├── dto/                             # Data Transfer Objects
│   │   ├── AttachmentDto.java
│   │   ├── CommentDto.java
│   │   ├── DeploymentDto.java
│   │   ├── EngineInfoDto.java
│   │   ├── HistoricProcessInstanceDto.java
│   │   ├── HistoricTaskInstanceDto.java
│   │   ├── ModelDto.java
│   │   ├── ProcessDefinitionDto.java
│   │   ├── ProcessInstanceDto.java
│   │   └── TaskDto.java
│   ├── exception/                       # Exception handling
│   │   └── GlobalExceptionHandler.java
│   ├── model/                           # Domain models
│   │   └── RetentionOffer.java
│   ├── repository/                      # JPA repositories
│   │   └── RetentionOfferRepository.java
│   ├── service/                         # Service interfaces
│   │   ├── FlowableDeploymentService.java
│   │   ├── FlowableDiagramService.java
│   │   ├── FlowableEngineInfoService.java
│   │   ├── FlowableHistoryService.java
│   │   ├── FlowableModelService.java
│   │   ├── FlowableProcessDefinitionService.java
│   │   ├── FlowableProcessService.java
│   │   ├── FlowableRuntimeService.java
│   │   ├── FlowableTaskService.java
│   │   └── RetentionOfferService.java
│   └── util/                            # Utility classes
│       ├── DateUtils.java
│       ├── DtoMapper.java
│       ├── ErrorHandlingUtils.java
│       └── ResponseUtils.java
└── src/main/resources/
    ├── application.properties           # Application configuration
    └── processes/                       # BPMN process definitions
        ├── retention-offer-process.bpmn
        └── generic-maker-checker-process.bpmn
```

---

## Core Architecture

### Application Entry Point

```java
@SpringBootApplication
@EnableCaching
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
```

**Key Features Enabled:**
- `@SpringBootApplication`: Auto-configuration, component scanning
- `@EnableCaching`: Caching support for improved performance

### Layered Architecture

```
┌─────────────────────────────────────┐
│     Frontend (React/Vite)           │
└──────────────┬──────────────────────┘
               │ HTTP/REST
┌──────────────▼──────────────────────┐
│   Controllers (REST Endpoints)      │
│   - Request validation              │
│   - Response formatting             │
│   - OpenAPI documentation           │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Service Layer                     │
│   - Business logic                  │
│   - Flowable API integration        │
│   - Transaction management          │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Flowable Engine                   │
│   - Process execution               │
│   - Task management                 │
│   - History tracking                │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Database (H2)                     │
│   - Process instances               │
│   - Task instances                  │
│   - History data                    │
│   - Business data                   │
└─────────────────────────────────────┘
```

---

## API Endpoints

### 1. Process Management APIs (`/api/process`)

**Controller:** `ProcessController.java`

| Method | Endpoint | Description | Request | Response |
|--------|----------|-------------|---------|----------|
| POST | `/start` | Start retention offer process | - | ProcessInstanceDto |
| POST | `/start/{processKey}` | Start process with variables | Map<String, Object> | ProcessInstanceDto |
| GET | `/active` | Get all active processes | - | List<ProcessInstanceDto> |
| GET | `/by-key/{processKey}` | Get processes by definition key | - | List<ProcessInstanceDto> |
| GET | `/{processInstanceId}` | Get specific process instance | - | ProcessInstanceDto |
| POST | `/suspend/{processInstanceId}` | Suspend process | - | status message |
| POST | `/activate/{processInstanceId}` | Activate suspended process | - | status message |
| DELETE | `/{processInstanceId}` | Delete process instance | - | status message |
| GET | `/tasks` | Get all process tasks | - | List<TaskDto> |
| GET | `/statistics` | Get process statistics | - | Map<String, Object> |

**Example Request - Start Process:**
```json
POST /api/process/start/retentionOfferProcess
{
  "customerId": "CUST001",
  "employeeName": "John Doe",
  "proposedSalary": 120000,
  "retentionBonus": 10000,
  "effectiveDate": "2024-03-01"
}
```

**Example Response:**
```json
{
  "id": "proc-12345",
  "processDefinitionId": "retentionOfferProcess:1:67890",
  "processDefinitionKey": "retentionOfferProcess",
  "businessKey": "OFFER-2024-001",
  "startTime": "2024-01-15T10:30:00Z",
  "status": "ACTIVE",
  "activeActivityIds": ["submitOfferTask"],
  "variables": { ... }
}
```

### 2. Task Management APIs (`/api/flowable/task`)

**Controller:** `FlowableTaskController.java`

| Method | Endpoint | Description | Request | Response |
|--------|----------|-------------|---------|----------|
| GET | `/user/{user}` | Get tasks for user | - | List<TaskDto> |
| POST | `/assign/{taskId}` | Claim/assign task | user (param) | void |
| POST | `/reassign/{taskId}` | Reassign task | newUser (param) | void |
| POST | `/delegate/{taskId}` | Delegate task | delegateUser (param) | void |
| POST | `/complete/{taskId}` | Complete task | Map<String, Object> | status message |

**Example - Complete Task:**
```json
POST /api/flowable/task/complete/task-123
{
  "managerApproval": "approve",
  "managerComments": "Approved for retention offer",
  "approvedAmount": 120000
}
```

### 3. Process Definition APIs (`/api/flowable/process-definition`)

**Controller:** `FlowableProcessDefinitionController.java`

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/` | Get all process definitions | List<ProcessDefinitionDto> |
| GET | `/latest` | Get latest versions only | List<ProcessDefinitionDto> |
| GET | `/key/{key}` | Get definition by key | ProcessDefinitionDto |
| GET | `/{id}` | Get definition by ID | ProcessDefinitionDto |

### 4. History & Audit APIs (`/api/flowable/history`)

**Controller:** `FlowableHistoryController.java`

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/process/{processKey}` | Get process history by key | List<HistoricProcessInstanceDto> |
| GET | `/process` | Get all process history | List<HistoricProcessInstanceDto> |
| GET | `/process/instance/{id}` | Get specific historic process | HistoricProcessInstanceDto |
| GET | `/task/process/{processInstanceId}` | Get task history for process | List<HistoricTaskInstanceDto> |
| GET | `/task/user/{user}` | Get task history for user | List<HistoricTaskInstanceDto> |
| GET | `/task/completed` | Get all completed tasks | List<HistoricTaskInstanceDto> |
| GET | `/process/statistics` | Get process statistics | Map<String, Object> |
| GET | `/task/statistics` | Get task statistics | Map<String, Object> |
| GET | `/process/date-range` | Get history by date range | List<HistoricProcessInstanceDto> |

### 5. Deployment APIs (`/api/flowable/deployment`)

**Controller:** `FlowableDeploymentController.java`

| Method | Endpoint | Description | Request | Response |
|--------|----------|-------------|---------|----------|
| GET | `/` | Get all deployments | - | List<DeploymentDto> |
| GET | `/{deploymentId}` | Get specific deployment | - | DeploymentDto |
| POST | `/upload` | Deploy from file upload | MultipartFile | DeploymentDto |
| POST | `/classpath` | Deploy from classpath | resourcePath, name | DeploymentDto |
| DELETE | `/{deploymentId}` | Delete deployment | cascade (param) | status message |
| GET | `/{deploymentId}/resources` | Get resource names | - | List<String> |
| GET | `/{deploymentId}/resources/{name}` | Download resource | - | byte[] |
| GET | `/statistics` | Get deployment statistics | - | Map<String, Object> |

### 6. Model Management APIs (`/api/models`)

**Controller:** `FlowableModelController.java`

| Method | Endpoint | Description | Response | Cached |
|--------|----------|-------------|----------|--------|
| GET | `/` | Get all models | List<ModelDto> | Yes |
| GET | `/{modelId}` | Get specific model | ModelDto | Yes |
| POST | `/` | Create new model | ModelDto | Cache evict |
| PUT | `/{modelId}` | Update model | ModelDto | Cache evict |
| DELETE | `/{modelId}` | Delete model | void | Cache evict |
| GET | `/{modelId}/source` | Get model source | byte[] | No |
| POST | `/{modelId}/source` | Update model source | void | Cache evict |
| GET | `/statistics` | Get model statistics | Map<String, Object> | Yes |

### 7. Engine Info APIs (`/api/flowable/engine`)

**Controller:** `FlowableEngineInfoController.java`

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/info` | Get engine information | EngineInfoDto |
| GET | `/properties` | Get engine properties | Map<String, Object> |
| GET | `/tables` | Get database tables | Map<String, Object> |
| GET | `/jobs/statistics` | Get job statistics | Map<String, Object> |
| GET | `/health` | Get engine health | Map<String, Object> |

### 8. Diagram/Visualization APIs (`/api/flowable/diagram`)

**Controller:** `FlowableDiagramController.java`

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/{processDefinitionKey}` | Get process diagram | InputStream |
| GET | `/{processDefinitionKey}/xml` | Get BPMN XML | String (XML) |

### 9. Runtime APIs (`/api/flowable/runtime`)

**Controller:** `FlowableRuntimeController.java`

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| POST | `/start/{processKey}` | Start process | processInstanceId |
| POST | `/suspend/{processInstanceId}` | Suspend process | void |
| POST | `/activate/{processInstanceId}` | Activate process | void |

### 10. Retention Offer APIs (`/api/retention-offers`)

**Controller:** `RetentionOfferController.java`

| Method | Endpoint | Description | Request | Response |
|--------|----------|-------------|---------|----------|
| POST | `/` | Create retention offer | RetentionOffer | RetentionOffer |
| GET | `/{id}` | Get offer by ID | - | RetentionOffer |
| GET | `/` | Get all offers | - | List<RetentionOffer> |
| PUT | `/{id}` | Update offer | RetentionOffer | RetentionOffer |
| DELETE | `/{id}` | Delete offer | - | void |

---

## Data Models & DTOs

### TaskDto Structure
```java
public class TaskDto {
    String id;                      // Task unique identifier
    String name;                    // Task name/title
    String description;             // Task description
    String assignee;                // Assigned user
    String owner;                   // Task owner
    String delegationState;         // PENDING, RESOLVED
    String processInstanceId;       // Parent process ID
    String processDefinitionId;     // Process definition
    String executionId;             // Execution context
    String taskDefinitionKey;       // Task definition key
    Date createTime;                // Creation timestamp
    Date dueDate;                   // Due date
    int priority;                   // Priority (default: 50)
    String category;                // Task category
    String formKey;                 // Associated form
    List<String> candidateUsers;    // Potential assignees
    List<String> candidateGroups;   // Candidate groups
    Map<String, Object> variables;  // Task variables
    List<String> comments;          // Task comments
    List<String> attachments;       // Task attachments
    String suspended;               // Suspension status
    String tenantId;                // Tenant identifier
}
```

### ProcessInstanceDto Structure
```java
public class ProcessInstanceDto {
    String id;                          // Process instance ID
    String processDefinitionId;         // Definition ID
    String processDefinitionKey;        // Definition key
    String businessKey;                 // Business identifier
    String startUserId;                 // Initiator
    String startTime;                   // Start timestamp
    String endTime;                     // End timestamp (if completed)
    String status;                      // ACTIVE, SUSPENDED, COMPLETED
    Map<String, Object> variables;      // Process variables
    List<String> activeActivityIds;     // Current activities
    List<String> completedActivityIds;  // Completed activities
    String diagramUrl;                  // Diagram URL
    String tenantId;                    // Tenant ID
    boolean suspended;                  // Suspension flag
    String name;                        // Process name
    String description;                 // Process description
}
```

### ProcessDefinitionDto Structure
```java
public class ProcessDefinitionDto {
    String id;              // Process definition ID
    String key;             // Process definition key
    String name;            // Process name
    String description;     // Process description
    int version;            // Version number
    String category;        // Category
    String deploymentId;    // Deployment ID
    String resourceName;    // BPMN file name
    String diagramResourceName; // Diagram file
    boolean suspended;      // Suspension status
    String tenantId;        // Tenant ID
}
```

### HistoricProcessInstanceDto
```java
public class HistoricProcessInstanceDto {
    String id;
    String processDefinitionId;
    String processDefinitionKey;
    String businessKey;
    String startUserId;
    Date startTime;
    Date endTime;
    Long durationInMillis;
    String deleteReason;
    Map<String, Object> variables;
    String tenantId;
}
```

### DeploymentDto
```java
public class DeploymentDto {
    String id;
    String name;
    Date deploymentTime;
    String category;
    String tenantId;
    List<String> resourceNames;
}
```

---

## Service Layer

### Service Interface Pattern

All services follow a consistent interface-implementation pattern:

```
service/
├── FlowableTaskService.java           (interface)
└── impl/
    └── FlowableTaskServiceImpl.java   (implementation)
```

### Key Services

#### 1. FlowableTaskService
**Responsibilities:**
- Task querying and retrieval
- Task assignment and delegation
- Task completion with variables
- Task reassignment

**Key Methods:**
```java
List<TaskDto> getTasksForUser(String user);
void claimTask(String taskId, String user);
void completeTask(String taskId, Map<String, Object> variables);
void reassignTask(String taskId, String newUser);
void delegateTask(String taskId, String delegateUser);
```

#### 2. FlowableProcessService
**Responsibilities:**
- Process instance creation
- Process instance querying
- Process suspension/activation
- Process deletion

#### 3. FlowableHistoryService
**Responsibilities:**
- Historic process instance retrieval
- Historic task instance retrieval
- Audit trail generation
- Statistics calculation

#### 4. FlowableDeploymentService
**Responsibilities:**
- Process deployment from files
- Process deployment from classpath
- Deployment deletion
- Resource management

#### 5. FlowableModelService
**Responsibilities:**
- Model CRUD operations
- Model source management
- Model statistics

---

## Process Workflows

### Retention Offer Approval Process

**BPMN File:** `retention-offer-process.bpmn`

**Process Flow:**

```
┌─────────────┐
│   Start     │
└──────┬──────┘
       │
       ▼
┌─────────────────────────┐
│ Submit Retention Offer  │ (User Task - HR Team)
│ - Employee Name         │
│ - Current Salary        │
│ - Proposed Salary       │
│ - Retention Bonus       │
│ - Effective Date        │
│ - Justification         │
└──────────┬──────────────┘
           │
           ▼
┌─────────────────────────┐
│   Manager Review        │ (User Task - Managers)
│ - Approve/Reject/Modify │
│ - Comments              │
└──────────┬──────────────┘
           │
           ├─ Approve ──────────────┐
           ├─ Reject ───────────────┤
           └─ Modify ───────────────┤
                                    │
                                    ▼
                            ┌───────────────┐
                            │  End Process  │
                            └───────────────┘
```

**Form Properties:**

**Submit Offer Task:**
- `employeeName` (string, required)
- `currentSalary` (long, required)
- `proposedSalary` (long, required)
- `retentionBonus` (long)
- `effectiveDate` (date, required)
- `justification` (string, required)

**Manager Review Task:**
- `managerApproval` (enum: approve/reject/modify, required)
- `managerComments` (string)

### Generic Maker-Checker Process

**BPMN File:** `generic-maker-checker-process.bpmn`

**Features:**
- Configurable approval steps
- Multi-level approval support
- Validation delegates
- Notification delegates

---

## Configuration

### Application Properties

**File:** `src/main/resources/application.properties`

```properties
# Database Configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=update

# Flowable Configuration
flowable.id-generator.datasource.enabled=false
flowable.process-definition-cache-limit=128
```

### CORS Configuration

**File:** `config/CorsConfig.java`

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

**Key Features:**
- All origins allowed (configurable)
- All HTTP methods supported
- Credentials allowed
- 1-hour cache for preflight requests

### Cache Configuration

**File:** `config/CacheConfig.java`

**Cached Resources:**
- Models (`models` cache)
- Individual model (`model` cache with key)
- Model statistics (`modelStatistics` cache)

**Cache Eviction:**
- On model create/update/delete
- On model source update

---

## Error Handling

### GlobalExceptionHandler

**File:** `exception/GlobalExceptionHandler.java`

**Exception Types Handled:**

#### 1. Flowable Exceptions
```java
@ExceptionHandler({
    FlowableException.class,
    FlowableObjectNotFoundException.class
})
```
- **HTTP Status:** 400 Bad Request
- **Error Code:** FLOWABLE_ERROR
- **Message:** Workflow-specific error details

#### 2. Validation Exceptions
```java
@ExceptionHandler({
    MethodArgumentNotValidException.class,
    BindException.class
})
```
- **HTTP Status:** 400 Bad Request
- **Error Code:** VALIDATION_ERROR
- **Message:** Validation failure details

#### 3. Type Mismatch
```java
@ExceptionHandler(MethodArgumentTypeMismatchException.class)
```
- **HTTP Status:** 400 Bad Request
- **Error Code:** TYPE_MISMATCH_ERROR
- **Message:** Parameter type mismatch details

#### 4. Runtime Exceptions
```java
@ExceptionHandler(RuntimeException.class)
```
- **HTTP Status:** 500 Internal Server Error (or specific based on message)
- **Error Code:** INTERNAL_ERROR / RESOURCE_NOT_FOUND / ACCESS_DENIED
- **Message:** Context-specific error message

**Error Response Format:**
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Validation Error",
  "message": "Invalid input provided. Please check your data.",
  "errorCode": "VALIDATION_ERROR",
  "path": "/api/process/start"
}
```

---

## Database Schema

### H2 In-Memory Database

**Console Access:** `http://localhost:8080/h2-console`

**JDBC URL:** `jdbc:h2:mem:testdb`

### Flowable Tables

Flowable engine creates and manages the following table categories:

#### 1. Process Definition Tables
- `ACT_RE_DEPLOYMENT` - Deployments
- `ACT_RE_PROCDEF` - Process definitions
- `ACT_RE_MODEL` - Process models

#### 2. Runtime Tables
- `ACT_RU_EXECUTION` - Process executions
- `ACT_RU_TASK` - Active tasks
- `ACT_RU_VARIABLE` - Runtime variables
- `ACT_RU_IDENTITYLINK` - Task assignments

#### 3. History Tables
- `ACT_HI_PROCINST` - Historic process instances
- `ACT_HI_TASKINST` - Historic task instances
- `ACT_HI_ACTINST` - Historic activity instances
- `ACT_HI_VARINST` - Historic variables
- `ACT_HI_DETAIL` - Historic detail events

#### 4. Identity Tables
- `ACT_ID_USER` - Users
- `ACT_ID_GROUP` - Groups
- `ACT_ID_MEMBERSHIP` - Group memberships

### Custom Application Tables

#### RetentionOffer Table
```sql
CREATE TABLE retention_offer (
    id BIGINT PRIMARY KEY,
    customer_id VARCHAR(255),
    rate DECIMAL,
    apy DECIMAL,
    effective_date DATE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

---

## Deployment Flow

### Process Deployment Lifecycle

```
┌────────────────────────────────┐
│  1. BPMN File Creation         │
│     - Design in modeler        │
│     - Define tasks & flows     │
│     - Add form properties      │
└──────────────┬─────────────────┘
               │
               ▼
┌────────────────────────────────┐
│  2. Deployment Options         │
│     a) Classpath (resources/)  │
│     b) File Upload (API)       │
│     c) Model API               │
└──────────────┬─────────────────┘
               │
               ▼
┌────────────────────────────────┐
│  3. Flowable Engine Parsing    │
│     - Validate BPMN XML        │
│     - Create process def       │
│     - Store in database        │
└──────────────┬─────────────────┘
               │
               ▼
┌────────────────────────────────┐
│  4. Process Available          │
│     - Accessible via API       │
│     - Can be instantiated      │
│     - Versioned automatically  │
└────────────────────────────────┘
```

### Deployment Methods

#### Method 1: Classpath Deployment (Auto-deployment)
```
resources/processes/
├── retention-offer-process.bpmn
└── generic-maker-checker-process.bpmn
```
- Automatically deployed on application startup
- Suitable for built-in processes

#### Method 2: API-based Deployment
```java
POST /api/flowable/deployment/upload
Content-Type: multipart/form-data
file: [BPMN file]
name: "My Process v2.0"
```

#### Method 3: Model-based Deployment
1. Create model via API
2. Update model source
3. Deploy from model

---

## Integration Points

### 1. Flowable Java Delegates

**Purpose:** Execute custom Java logic during process execution

**Example: ValidationDelegate**

```java
@Component("validationDelegate")
public class ValidationDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        // Custom validation logic
        Map<String, Object> data = (Map) execution.getVariable("retentionOfferData");
        boolean isValid = validateOfferData(data);
        execution.setVariable("validationPassed", isValid);
    }
}
```

**Available Delegates:**
- `ValidationDelegate` - Data validation
- `NotificationDelegate` - Email/notification sending
- `DbUpdateDelegate` - Database updates

**BPMN Integration:**
```xml
<serviceTask id="validateTask" 
             name="Validate Offer" 
             flowable:delegateExpression="${validationDelegate}"/>
```

### 2. Service Task Integration

**Execution Points:**
- After user task completion
- Before critical decision gateways
- For automated notifications
- For database synchronization

### 3. External System Integration

**Patterns:**
- **REST API Calls:** Via service tasks or delegates
- **Database Updates:** Via JPA repositories
- **Message Queues:** Can be integrated via delegates
- **Email/SMS:** Via notification delegates

### 4. Frontend Integration

**Communication:**
- REST API over HTTP
- JSON payloads
- CORS-enabled for cross-origin requests
- Token-based authentication (can be added)

**Key Frontend Operations:**
1. Start process instance
2. Fetch user tasks
3. Complete tasks with form data
4. View process history
5. Monitor process diagrams

---

## Process Execution Flow

### Typical Workflow Execution

```
1. Frontend triggers process start
   POST /api/process/start/retentionOfferProcess
   
2. Backend creates process instance
   - Flowable engine initializes process
   - First task becomes active
   
3. Task assignment
   - Task assigned to candidate group/user
   - Appears in task list
   
4. User claims task
   POST /api/flowable/task/assign/{taskId}?user=john.doe
   
5. User completes task with data
   POST /api/flowable/task/complete/{taskId}
   {
     "employeeName": "John Doe",
     "proposedSalary": 120000
   }
   
6. Flowable engine processes:
   - Executes service tasks (delegates)
   - Evaluates gateways
   - Creates next tasks
   
7. Process continues until end event
   
8. History stored for audit
   GET /api/flowable/history/process/{processKey}
```

---

## Key Technical Details

### 1. DTO Mapping Pattern
```java
private TaskDto toDto(Task task) {
    TaskDto dto = new TaskDto();
    dto.id = task.getId();
    dto.name = task.getName();
    dto.assignee = task.getAssignee();
    // ... map all fields
    return dto;
}
```

### 2. Flowable API Usage

**Task Service:**
```java
@Autowired
private TaskService taskService;

List<Task> tasks = taskService.createTaskQuery()
    .taskCandidateOrAssigned(user)
    .list();
```

**Runtime Service:**
```java
@Autowired
private RuntimeService runtimeService;

ProcessInstance instance = runtimeService
    .startProcessInstanceByKey("retentionOfferProcess", variables);
```

**History Service:**
```java
@Autowired
private HistoryService historyService;

List<HistoricProcessInstance> history = historyService
    .createHistoricProcessInstanceQuery()
    .processDefinitionKey(processKey)
    .list();
```

### 3. Transaction Management

- Spring manages transactions automatically
- `@Transactional` annotation can be added for complex operations
- Flowable operations are transactional by default

### 4. Caching Strategy

**Models cached to reduce DB queries:**
```java
@Cacheable("models")
public List<ModelDto> getAllModels() { ... }

@CacheEvict(value = "models", allEntries = true)
public ModelDto createModel(...) { ... }
```

---

## API Documentation (Swagger)

### Access
**URL:** `http://localhost:8080/swagger-ui.html`

**Features:**
- Interactive API testing
- Request/response schemas
- Authentication testing
- Example requests

### OpenAPI Configuration

Annotations used:
- `@Tag` - Controller-level descriptions
- `@Operation` - Endpoint descriptions
- `@ApiResponses` - Response documentation
- `@Parameter` - Parameter descriptions
- `@Schema` - DTO documentation

---

## Security Considerations

### Current Implementation
- **No authentication** (suitable for development)
- CORS configured for open access
- All endpoints publicly accessible

### Production Recommendations
1. Add Spring Security
2. Implement JWT/OAuth2 authentication
3. Role-based access control (RBAC)
4. Restrict CORS to specific origins
5. Enable HTTPS
6. Add request rate limiting
7. Implement audit logging

---

## Performance Optimizations

### 1. Caching
- Model data cached in memory
- Cache eviction on updates
- Reduces database queries

### 2. Process Definition Cache
```properties
flowable.process-definition-cache-limit=128
```
- Caches up to 128 process definitions
- Faster process instantiation

### 3. Database Connection Pooling
- Spring Boot default HikariCP
- Optimized for high concurrency

### 4. Lazy Loading
- Variables loaded on demand
- Reduces initial query overhead

---

## Monitoring & Diagnostics

### Available Endpoints

**Engine Health:**
```
GET /api/flowable/engine/health
```

**Engine Statistics:**
```
GET /api/flowable/engine/info
GET /api/process/statistics
GET /api/flowable/history/process/statistics
```

**Job Statistics:**
```
GET /api/flowable/engine/jobs/statistics
```

**Database Tables:**
```
GET /api/flowable/engine/tables
```

---

## Extension Points

### Adding New Process

1. **Create BPMN file**
   - Place in `resources/processes/`
   - Define user tasks, service tasks, gateways

2. **Create delegates (if needed)**
   ```java
   @Component("myDelegate")
   public class MyDelegate implements JavaDelegate {
       @Override
       public void execute(DelegateExecution execution) {
           // Custom logic
       }
   }
   ```

3. **Reference in BPMN**
   ```xml
   <serviceTask flowable:delegateExpression="${myDelegate}"/>
   ```

4. **Restart application**
   - Process auto-deployed

### Adding New REST Endpoint

1. **Create DTO (if needed)**
2. **Add service method**
3. **Add controller endpoint**
   ```java
   @GetMapping("/my-endpoint")
   @Operation(summary = "My endpoint")
   public MyDto getMyData() {
       return myService.getData();
   }
   ```

---

## Development Workflow

### Running the Application

```bash
# Maven
mvn spring-boot:run

# Or build and run
mvn clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

**Access Points:**
- Application: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 Console: `http://localhost:8080/h2-console`

### Testing

```bash
# Run tests
mvn test

# Skip tests during build
mvn clean package -DskipTests
```

---

## Common Use Cases

### 1. Start a Process Instance
```bash
curl -X POST http://localhost:8080/api/process/start/retentionOfferProcess \
  -H "Content-Type: application/json" \
  -d '{
    "employeeName": "John Doe",
    "proposedSalary": 120000
  }'
```

### 2. Get User Tasks
```bash
curl http://localhost:8080/api/flowable/task/user/john.doe
```

### 3. Complete a Task
```bash
curl -X POST http://localhost:8080/api/flowable/task/complete/task-123 \
  -H "Content-Type: application/json" \
  -d '{
    "managerApproval": "approve",
    "comments": "Approved"
  }'
```

### 4. View Process History
```bash
curl http://localhost:8080/api/flowable/history/process/retentionOfferProcess
```

---

## Troubleshooting

### Common Issues

**1. Process not found**
- Check deployment: `GET /api/flowable/deployment`
- Verify BPMN file in `resources/processes/`

**2. Task not appearing**
- Verify candidate group/user assignment
- Check process instance status
- Review process diagram

**3. Delegate not executing**
- Ensure `@Component` annotation
- Verify bean name matches BPMN reference
- Check application logs

**4. CORS errors**
- Verify frontend origin in CorsConfig
- Check browser console for details

---

## Summary

This backend provides a **comprehensive BPM platform** with:

✅ **Complete REST APIs** for all Flowable operations  
✅ **Maker-checker workflow support**  
✅ **Extensive history and audit capabilities**  
✅ **Flexible deployment options**  
✅ **Production-ready error handling**  
✅ **OpenAPI documentation**  
✅ **Caching for performance**  
✅ **Extensible delegate system**  

**Ideal for:** Workflow automation, approval processes, audit trails, business process management.

**Next Steps for Production:**
1. Add authentication/authorization
2. Configure production database (PostgreSQL/MySQL)
3. Enable HTTPS
4. Add monitoring/logging integration
5. Configure external notification services
6. Implement backup/recovery strategy

---

## Contact & Support

For questions about this backend implementation, refer to:
- Flowable Documentation: https://www.flowable.com/open-source/docs
- Spring Boot Documentation: https://spring.io/projects/spring-boot
- OpenAPI Specification: https://swagger.io/specification/

---

*Last Updated: 2024*
*Version: 1.0*
