# Flowable Process Management Technical Documentation

**Version:** 7.2.0 (Open Source)  
**Implementation:** Spring Boot Process Engine  
**Use Case:** Maker-Checker Approval Workflow  
**Last Updated:** January 20, 2026

---

## Table of Contents

1. [Overview](#1-overview)
2. [Architecture](#2-architecture)
3. [Database Tables](#3-database-tables)
4. [Flowable Services](#4-flowable-services)
5. [BPMN Process Diagrams](#5-bpmn-process-diagrams)
6. [Task Listeners](#6-task-listeners)
7. [Form Keys for Frontend Routing](#7-form-keys-for-frontend-routing)
8. [Process Variables](#8-process-variables)
9. [Configuration](#9-configuration)
10. [Best Practices](#10-best-practices)
11. [Troubleshooting](#11-troubleshooting)

---

## 1. Overview

### 1.1 What is Flowable?

Flowable is a lightweight, open-source business process engine written in Java. It implements the BPMN 2.0 specification and provides:

- **Process Engine**: Core workflow execution
- **Task Management**: Human task handling with assignments and delegation
- **History Service**: Complete audit trail of all process activities
- **Event Handling**: Task listeners, execution listeners, and event handlers

### 1.2 Implementation Approach

Using Flowable **7.2.0** as a Spring Boot embedded library:

| Component | Description |
|-----------|-------------|
| **Process Engine** | Core BPMN execution engine |
| **Spring Boot Starter** | Auto-configuration for Spring Boot |
| **Database** | Backend storage for Flowable tables (supports PostgreSQL, MySQL, Oracle, H2, etc.) |
| **Custom Services** | Application-specific business logic layered on top |

### 1.3 Dependencies

**Required Maven Dependencies:**
- `org.flowable:flowable-spring-boot-starter-process:7.2.0` - Core process engine starter
- `org.flowable:flowable-spring-boot-starter:7.2.0` - General Spring Boot integration

---

## 2. Architecture

### 2.1 Component Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              FRONTEND                                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │ Maker Pages │  │Checker Pages│  │ Admin Pages │  │  Dashboard  │         │
│  │  (formKey)  │  │  (formKey)  │  │  (formKey)  │  │             │         │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘         │
└─────────┼────────────────┼────────────────┼────────────────┼────────────────┘
          │                │                │                │
          ▼                ▼                ▼                ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        SPRING BOOT BACKEND                                   │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │                         REST CONTROLLERS                                │ │
│  │  TaskController  │  ProcessController  │  DeploymentController          │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                    │                                         │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │                         CUSTOM SERVICES                                 │ │
│  │  TaskManagementService  │  ProcessManagementService  │  BusinessService │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                    │                                         │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │                       FLOWABLE ENGINE SERVICES                          │ │
│  │  RuntimeService │ TaskService │ HistoryService │ RepositoryService      │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                    │                                         │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │                         TASK LISTENERS                                  │ │
│  │  Custom Task Listeners for Business Logic Execution                     │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              DATABASE                                        │
│  ┌────────────────────────┐    ┌────────────────────────┐                   │
│  │   FLOWABLE TABLES      │    │   APPLICATION TABLES   │                   │
│  │  (ACT_* prefix)        │    │  (Business Data)       │                   │
│  └────────────────────────┘    └────────────────────────┘                   │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 Maker-Checker Flow Concept

The Maker-Checker pattern is a dual-control mechanism where:
- **Maker**: Creates or modifies data
- **Checker**: Reviews and approves/rejects the changes

```
                                    ┌─────────┐
                                    │  START  │
                                    └────┬────┘
                                         │
                    ┌────────────────────▼────────────────────┐
                    │            STAGE N                       │
    ┌───────────────┴───────────────┐                         │
    │                               │                         │
    ▼                               │                         │
┌──────────────┐                    │                         │
│ MAKER TASK   │◄───────────────────┼─────────────────────────┤
│              │     REJECT         │                         │
└──────┬───────┘                    │                         │
       │ Submit                     │                         │
       ▼                            │                         │
┌──────────────┐    APPROVE         │                         │
│ CHECKER TASK │────────────────────┘                         │
│              │                                              │
└──────┬───────┘                                              │
       │                                                      │
       ▼                                                      │
┌────────────────────────────────────────────────────────────┐│
│            STAGE N-1                                       ││
│   (Same pattern repeats)                                   ││
└────────────────────────────────────────────────────────────┘│
       │                                                      │
       ▼                                                      │
┌──────────────────┐                                          │
│ FINAL PROCESSING │                                          │
│   (Optional)     │                                          │
└────────┬─────────┘                                          │
         │                                                    │
         ▼                                                    │
    ┌─────────┐                                               │
    │   END   │                                               │
    └─────────┘                                               │
```

---

## 3. Database Tables

### 3.1 Overview

When Flowable starts, it automatically creates a set of database tables prefixed with `ACT_`. These tables store all workflow-related data including process definitions, running instances, tasks, variables, and historical records.

**Table Naming Convention:**
- `ACT_` = Activiti/Flowable prefix (historical naming from Activiti project)
- Second part indicates the category:
  - `GE_` = **G**eneral - shared resources and properties
  - `RE_` = **R**epository - static process definition data
  - `RU_` = **RU**ntime - dynamic data for running process instances
  - `HI_` = **HI**story - historical/audit data
  - `ID_` = **ID**entity - user and group information

**Important:** These tables are managed by Flowable's internal mechanisms. **Never modify these tables directly** via SQL as it can corrupt the workflow state.

### 3.2 Flowable Core Tables (ACT_* Prefix)

#### 3.2.1 General Tables (ACT_GE_*)

These tables store shared resources and engine-level properties that are used across all Flowable components.

| Table | Description | Purpose |
|-------|-------------|---------|
| `ACT_GE_PROPERTY` | Engine properties and schema version | Stores key-value pairs for engine configuration, including the database schema version. Flowable checks this table on startup to determine if schema migration is needed. |
| `ACT_GE_BYTEARRAY` | Binary content storage | Stores all binary data including deployed BPMN XML files, process diagram images (PNG), and serialized Java objects used as variables. Referenced by deployment and variable tables. |

#### 3.2.2 Repository Tables (ACT_RE_*)

These tables store **static** process definition data. The data here doesn't change during process execution - it represents the "blueprint" of your workflows.

| Table | Description | Purpose |
|-------|-------------|---------|
| `ACT_RE_DEPLOYMENT` | Deployment metadata | Each time you deploy a BPMN file, a record is created here. Contains deployment name, timestamp, and tenant information. Multiple process definitions can belong to one deployment. |
| `ACT_RE_PROCDEF` | Process definition metadata | Stores parsed process definition information including process key, name, version, and reference to deployment. Flowable automatically versions process definitions - each deployment of the same process key creates a new version. |
| `ACT_RE_MODEL` | Model storage (optional) | Used by Flowable Modeler application to store process models. Not typically used when deploying BPMN files directly. |

#### 3.2.3 Runtime Tables (ACT_RU_*)

These tables store **dynamic** data for currently executing process instances. Data is constantly created, updated, and deleted as processes execute. When a process completes, its runtime data is removed (moved to history tables if history is enabled).

| Table | Description | Purpose |
|-------|-------------|---------|
| `ACT_RU_EXECUTION` | Process instance and execution data | The core runtime table. Each running process instance has a root execution record. Sub-processes and parallel branches create additional execution records forming a tree structure. |
| `ACT_RU_TASK` | Active user tasks | Stores all currently active human tasks waiting for user action. Contains task name, assignee, candidate groups, form key, due date, and priority. Deleted when task is completed. |
| `ACT_RU_VARIABLE` | Runtime process/task variables | Stores all variables attached to process instances or tasks. Supports multiple data types (string, long, double, date, serializable). Variables are key-value pairs that drive process logic. |
| `ACT_RU_IDENTITYLINK` | Task assignments | Links users and groups to tasks. Types include: `assignee` (directly assigned), `candidate` (can claim), `owner` (delegated from), `participant` (involved user). |
| `ACT_RU_JOB` | Asynchronous jobs | Stores jobs waiting to be executed by the async executor. Includes async service tasks, timer events, and retry attempts for failed jobs. |
| `ACT_RU_TIMER_JOB` | Timer jobs | Specifically stores timer events (delays, due dates) waiting to fire. The async executor monitors this table and moves jobs to ACT_RU_JOB when their time arrives. |
| `ACT_RU_SUSPENDED_JOB` | Suspended jobs | Jobs that are paused because their process instance is suspended. Moved back to normal job tables when process is activated. |
| `ACT_RU_DEADLETTER_JOB` | Failed jobs (dead letter) | Jobs that have exceeded their retry limit end up here. Requires manual intervention to investigate and retry. Contains exception message and stack trace. |
| `ACT_RU_HISTORY_JOB` | History async jobs | Jobs related to asynchronous history data processing. Used when async history is enabled for performance optimization. |
| `ACT_RU_EVENT_SUBSCR` | Event subscriptions | Stores active subscriptions to BPMN events like signals, messages, and conditional events. When an event is triggered, Flowable queries this table to find matching subscriptions. |
| `ACT_RU_ACTINST` | Activity instances | Tracks which activities (tasks, gateways, events) are currently active within each execution. Used for process state tracking and diagram highlighting. |

#### 3.2.4 History Tables (ACT_HI_*)

These tables store **audit/historical** data. Unlike runtime tables, data here is never deleted automatically (unless you configure cleanup jobs). This provides a complete audit trail of all process activities.

| Table | Description | Purpose |
|-------|-------------|---------|
| `ACT_HI_PROCINST` | Historic process instances | One record per process instance ever started. Contains start time, end time, duration, initiator, and delete reason (if terminated early). The primary table for process analytics. |
| `ACT_HI_ACTINST` | Historic activity instances | Records every activity (task, gateway, event) visited by every process instance. Shows the complete execution path with timestamps. Essential for process mining and optimization. |
| `ACT_HI_TASKINST` | Historic task instances | Complete history of all user tasks including who claimed them, when they were completed, and how long they took. Critical for workload analysis and SLA monitoring. |
| `ACT_HI_VARINST` | Historic variable values | Stores the final value of each process variable. For full variable change history, use ACT_HI_DETAIL with history level set to "full". |
| `ACT_HI_IDENTITYLINK` | Historic identity links | Preserves the assignment history - who was assigned, who were candidates. Useful for audit trails showing who was responsible for each task. |
| `ACT_HI_DETAIL` | Variable change details | Stores every change to every variable (when history level is "full"). Can grow very large but provides complete variable audit trail. |
| `ACT_HI_COMMENT` | Task/process comments | Stores comments added by users to tasks or process instances. Useful for collaboration and documenting decisions. |
| `ACT_HI_ATTACHMENT` | Task attachments | Metadata for files attached to tasks. The actual file content is stored in ACT_GE_BYTEARRAY. |

#### 3.2.5 Identity Tables (ACT_ID_*)

These tables provide Flowable's built-in identity management. However, **when integrating with Spring Security or external identity providers, these tables are typically NOT used**. Instead, you manage users externally and pass user/group information to Flowable via the API.

| Table | Description | Purpose |
|-------|-------------|---------|
| `ACT_ID_USER` | User accounts | Stores user information if using Flowable's built-in identity management. Contains user ID, name, email, and password hash. |
| `ACT_ID_GROUP` | User groups | Stores group definitions that can be used for task candidate groups. Groups can have types (e.g., "security-role", "assignment"). |
| `ACT_ID_MEMBERSHIP` | User-group mapping | Many-to-many relationship between users and groups. Determines which groups a user belongs to. |
| `ACT_ID_INFO` | User properties | Additional key-value properties for users. Can store custom attributes like department, phone number, etc. |
| `ACT_ID_TOKEN` | Authentication tokens | Stores tokens for programmatic authentication. Used by Flowable REST API for token-based auth. |
| `ACT_ID_PRIV` | Privileges | Defines system-level privileges that can be assigned to users or groups. |
| `ACT_ID_PRIV_MAPPING` | Privilege assignments | Maps privileges to users or groups. Controls access to Flowable admin functions. |

### 3.3 Table Relationships Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         REPOSITORY (Definitions)                             │
│                                                                              │
│    ACT_RE_DEPLOYMENT ─────┬───────▶ ACT_RE_PROCDEF                          │
│         │                 │              │                                   │
│         ▼                 ▼              │                                   │
│    ACT_GE_BYTEARRAY   ACT_GE_PROPERTY    │                                   │
│    (BPMN XML stored)                     │                                   │
└──────────────────────────────────────────┼───────────────────────────────────┘
                                           │
                    ┌──────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         RUNTIME (Active Processes)                           │
│                                                                              │
│    ACT_RU_EXECUTION ◄──────────────────────────────────────────────────────┐│
│         │                                                                   ││
│         ├──────▶ ACT_RU_TASK ─────────▶ ACT_RU_IDENTITYLINK                ││
│         │            │                                                      ││
│         │            ▼                                                      ││
│         ├──────▶ ACT_RU_VARIABLE                                           ││
│         │                                                                   ││
│         ├──────▶ ACT_RU_JOB / ACT_RU_TIMER_JOB                             ││
│         │                                                                   ││
│         └──────▶ ACT_RU_EVENT_SUBSCR                                       ││
│                                                                   │         │
└───────────────────────────────────────────────────────────────────┼─────────┘
                                                                    │
                    On Process Completion ──────────────────────────┘
                                           │
                                           ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         HISTORY (Completed Processes)                        │
│                                                                              │
│    ACT_HI_PROCINST ◄───────────────────────────────────────────────────────┐│
│         │                                                                   ││
│         ├──────▶ ACT_HI_ACTINST                                            ││
│         │                                                                   ││
│         ├──────▶ ACT_HI_TASKINST ─────────▶ ACT_HI_IDENTITYLINK            ││
│         │            │                                                      ││
│         │            ├──────▶ ACT_HI_COMMENT                               ││
│         │            └──────▶ ACT_HI_ATTACHMENT                            ││
│         │                                                                   ││
│         ├──────▶ ACT_HI_VARINST                                            ││
│         │                                                                   ││
│         └──────▶ ACT_HI_DETAIL                                             ││
│                                                                             ││
└─────────────────────────────────────────────────────────────────────────────┘
```

### 3.3 Key Table Queries

**Useful SQL queries for debugging and monitoring:**

| Query Purpose | Primary Tables |
|---------------|----------------|
| View deployed process definitions | `ACT_RE_PROCDEF` - filter by KEY_, VERSION_ |
| View running process instances | `ACT_RU_EXECUTION` - join with ACT_RE_PROCDEF for names |
| View active tasks | `ACT_RU_TASK` - check ASSIGNEE_, FORM_KEY_ |
| View claimable tasks | `ACT_RU_TASK` + `ACT_RU_IDENTITYLINK` - where ASSIGNEE_ is NULL |
| View process variables | `ACT_RU_VARIABLE` - filter by PROC_INST_ID_ |
| View completed processes | `ACT_HI_PROCINST` - where END_TIME_ is not NULL |

---

## 4. Flowable Services

### 4.1 Overview

Flowable provides a set of core services that expose the full functionality of the process engine. These services are the primary API for interacting with Flowable - you should **never interact with the database tables directly**. Instead, use these services to start processes, manage tasks, query data, and control the engine.

When using Spring Boot, these services are automatically configured and can be injected into your beans using `@Autowired`. Simply inject `RuntimeService`, `TaskService`, `HistoryService`, `RepositoryService`, or `ManagementService` into your service classes.

### 4.2 Core Services Detailed

#### 4.2.1 RepositoryService

**Purpose:** Manages process definitions and deployments. This is your gateway for deploying BPMN files and querying what processes are available.

**When to use:**
- Deploying new BPMN process definitions
- Querying available process definitions
- Getting process definition metadata (name, key, version)
- Retrieving BPMN XML or process diagrams
- Deleting deployments

**Key Concepts:**
- **Deployment**: A container for one or more resources (BPMN files, images). Each deployment is immutable.
- **Process Definition**: Parsed BPMN process stored in the engine. Has a key (from BPMN id), name, and auto-incremented version.
- **Versioning**: Deploying the same process key creates a new version. Old versions remain available for running instances.

**Key Methods:**

| Method | Description |
|--------|-------------|
| `createDeployment()` | Creates a new deployment builder to add resources and deploy |
| `createProcessDefinitionQuery()` | Query process definitions with filters and sorting |
| `getBpmnModel(processDefinitionId)` | Get the BPMN model object for programmatic inspection |
| `getResourceAsStream(deploymentId, resourceName)` | Get raw resource (BPMN XML, diagram image) |
| `deleteDeployment(deploymentId, cascade)` | Delete deployment (cascade=true also deletes running instances) |
| `suspendProcessDefinitionById(id)` | Prevent new instances from being started |
| `activateProcessDefinitionById(id)` | Re-enable starting new instances |

---

#### 4.2.2 RuntimeService

**Purpose:** Controls process instance execution. This is your primary service for starting processes, managing running instances, and working with process variables.

**When to use:**
- Starting new process instances
- Querying running process instances
- Setting and getting process variables
- Suspending/activating process instances
- Deleting (canceling) process instances
- Sending signals or messages to processes

**Key Concepts:**
- **Process Instance**: A running instance of a process definition. Has its own ID, variables, and execution state.
- **Execution**: The actual "token" moving through the process. A process instance can have multiple executions (parallel branches).
- **Business Key**: Optional identifier linking the process instance to your business data (e.g., order ID).
- **Variables**: Key-value data attached to the process instance that persists throughout execution.

**Key Methods:**

| Method | Description |
|--------|-------------|
| `startProcessInstanceByKey(key)` | Start a process using its definition key (uses latest version) |
| `startProcessInstanceByKey(key, businessKey, variables)` | Start with business correlation and initial variables |
| `createProcessInstanceQuery()` | Query running instances with filters |
| `suspendProcessInstanceById(id)` | Pause execution (tasks cannot be completed) |
| `activateProcessInstanceById(id)` | Resume a suspended process |
| `deleteProcessInstance(id, reason)` | Cancel/terminate a process instance |
| `setVariable(executionId, name, value)` | Set a process variable |
| `getVariable(executionId, name)` | Get a process variable |
| `getVariables(executionId)` | Get all variables as a Map |
| `signalEventReceived(signalName)` | Broadcast a signal to all listening processes |
| `messageEventReceived(messageName, executionId)` | Send a message to a specific waiting execution |

---

#### 4.2.3 TaskService

**Purpose:** Manages human tasks (User Tasks in BPMN). This is your primary service for task inbox functionality, task assignment, and task completion.

**When to use:**
- Querying tasks for a user or group
- Claiming tasks (assigning to yourself)
- Completing tasks with outcome variables
- Delegating or reassigning tasks
- Adding comments or attachments to tasks
- Setting task-local variables

**Key Concepts:**
- **User Task**: A BPMN task that requires human action. Creates a record in ACT_RU_TASK.
- **Assignee**: The user currently responsible for the task. Only the assignee can complete it.
- **Candidate Users/Groups**: Users or groups who can claim the task. The task appears in their inbox.
- **Claiming**: Taking ownership of a task from the candidate pool. Removes other candidates' ability to claim.
- **Task Variables**: Variables scoped to the task (local) or visible to the process (non-local).

**Key Methods:**

| Method | Description |
|--------|-------------|
| `createTaskQuery()` | Query tasks with extensive filter options |
| `claim(taskId, userId)` | Assign the task to yourself |
| `unclaim(taskId)` | Release task back to candidate pool |
| `complete(taskId)` | Complete task without variables |
| `complete(taskId, variables)` | Complete task and set process variables (affects gateway routing) |
| `setAssignee(taskId, userId)` | Directly assign to a user (admin operation) |
| `delegateTask(taskId, userId)` | Delegate to another user (original owner tracked) |
| `addComment(taskId, processInstanceId, message)` | Add a comment visible in task history |
| `getVariable(taskId, name)` | Get a variable accessible to this task |
| `setVariable(taskId, name, value)` | Set a process variable via the task |
| `setVariableLocal(taskId, name, value)` | Set a task-scoped variable (not visible to process) |

---

#### 4.2.4 HistoryService

**Purpose:** Queries historical/audit data. Use this for reporting, analytics, and auditing completed processes and tasks.

**When to use:**
- Querying completed process instances
- Finding task history (who did what, when)
- Retrieving historical variable values
- Process mining and analytics
- Audit trail generation
- Performance analysis (durations, bottlenecks)

**Key Concepts:**
- **History Level**: Controls what data is recorded (none, activity, audit, full). Set in configuration.
- **Historic Process Instance**: Record of a completed (or running) process with timestamps and duration.
- **Historic Task Instance**: Record of a completed task including assignee and completion time.
- **Historic Activity Instance**: Record of each BPMN element visited during execution.

**Key Methods:**

| Method | Description |
|--------|-------------|
| `createHistoricProcessInstanceQuery()` | Query past process instances |
| `createHistoricTaskInstanceQuery()` | Query past task instances |
| `createHistoricActivityInstanceQuery()` | Query activity execution history |
| `createHistoricVariableInstanceQuery()` | Query variable values |
| `deleteHistoricProcessInstance(id)` | Delete all history for a process (cleanup) |

---

#### 4.2.5 ManagementService

**Purpose:** Administrative and monitoring operations. Use this for job management, database inspection, and engine maintenance.

**When to use:**
- Monitoring and retrying failed jobs
- Inspecting database table metadata
- Executing custom SQL (advanced)
- Managing timer jobs
- Engine maintenance operations

**Key Methods:**

| Method | Description |
|--------|-------------|
| `createJobQuery()` | Query pending async jobs |
| `createTimerJobQuery()` | Query timer jobs waiting to fire |
| `createDeadLetterJobQuery()` | Query failed jobs in dead letter queue |
| `executeJob(jobId)` | Manually execute a job |
| `moveJobToDeadLetterJob(jobId)` | Move failed job to dead letter |
| `moveDeadLetterJobToExecutableJob(jobId)` | Retry a dead letter job |
| `getTableMetaData(tableName)` | Get column info for a Flowable table |
| `getTableCount()` | Get row counts for all tables |

---

#### 4.2.6 IdentityService (Optional)

**Purpose:** Manages users and groups within Flowable's built-in identity system.

**When to use:** Only if you're using Flowable's internal identity management instead of Spring Security or external identity providers.

> **Note:** In most Spring Boot applications, you'll manage users externally (via Spring Security, LDAP, OAuth, etc.) and simply pass the current user's ID and groups to Flowable. The IdentityService is then not needed.

**Key Methods:**

| Method | Description |
|--------|-------------|
| `newUser(userId)` | Create a new user |
| `newGroup(groupId)` | Create a new group |
| `createMembership(userId, groupId)` | Add user to group |
| `setAuthenticatedUserId(userId)` | Set the current user for audit purposes |

**Note:** Use `setAuthenticatedUserId()` before starting a process to record the initiator in the audit trail. Always clear it afterwards.

---

### 4.3 Service Method Summary

| Service | Primary Responsibility | Key Tables Affected |
|---------|----------------------|-------------------|
| **RepositoryService** | Process definitions & deployments | ACT_RE_*, ACT_GE_BYTEARRAY |
| **RuntimeService** | Running process instances | ACT_RU_EXECUTION, ACT_RU_VARIABLE |
| **TaskService** | Human task management | ACT_RU_TASK, ACT_RU_IDENTITYLINK |
| **HistoryService** | Audit & historical queries | ACT_HI_* (read-only) |
| **ManagementService** | Jobs & administration | ACT_RU_JOB, ACT_RU_DEADLETTER_JOB |
---

## 5. BPMN Elements Reference

BPMN (Business Process Model and Notation) is the standard notation for defining workflows. Here are the key elements used in process definitions:

### 5.1 BPMN 2.0 Elements

| Element | Symbol | Description |
|---------|--------|-------------|
| **Start Event** | ○ | Process entry point - every process must have at least one |
| **End Event** | ◉ | Process termination point - marks process completion |
| **User Task** | ▭ | Human task requiring user action (appears in task inbox) |
| **Service Task** | ⚙ | Automated task execution (Java code, REST call, etc.) |
| **Script Task** | 📜 | Execute inline script (JavaScript, Groovy) |
| **Exclusive Gateway** | ◇ | Decision point (XOR) - exactly one outgoing path selected based on conditions |
| **Parallel Gateway** | ◇+ | Fork/join for parallel execution - all paths execute simultaneously |
| **Inclusive Gateway** | ◇○ | One or more paths selected based on conditions |
| **Sequence Flow** | → | Connection between elements defining execution order |
| **Timer Event** | ⏰ | Trigger based on time (delay, specific date, recurring) |
| **Message Event** | ✉ | Trigger based on receiving a message |
| **Signal Event** | 📡 | Broadcast trigger that multiple processes can catch |

### 5.2 Key BPMN Attributes for User Tasks

| Attribute | Purpose | Example |
|-----------|---------|---------|
| `id` | Unique identifier for the element | `makerTask` |
| `name` | Display name shown in task list | `"Submit Request"` |
| `flowable:formKey` | Form identifier for UI routing | `/maker/submit` |
| `flowable:candidateGroups` | Groups that can claim the task | `MAKER`, `CHECKER` |
| `flowable:assignee` | Specific user assignment (expression allowed) | `${initiator}` |
| `flowable:candidateUsers` | Specific users who can claim | `user1,user2` |
| `flowable:dueDate` | Task due date expression | `${dueDate}` |
| `flowable:priority` | Task priority (0-100) | `50` |
| `flowable:taskListener` | Event handler for task lifecycle | `${taskListener}` |

### 5.3 Gateway Conditions

Gateway conditions use expression language (`${}`) to evaluate process variables:

| Condition Type | Example Expression | Description |
|----------------|-------------------|-------------|
| Simple equality | `${decision == 'APPROVE'}` | Check variable equals value |
| Multiple conditions | `${decision == 'APPROVE' && amount > 1000}` | Combine with AND/OR |
| Default flow | No condition specified | Used when no other condition matches |

---

## 6. Task Listeners

Task listeners are event handlers that execute custom logic during the lifecycle of a user task. They enable you to hook into task events and perform actions like setting default values, sending notifications, logging, or validating data.

### 6.1 Task Listener Events

| Event | When Triggered | Common Use Cases |
|-------|----------------|------------------|
| `create` | When task is created | Set default values, initialize variables, send notifications |
| `assignment` | When task is assigned or claimed | Audit logging, permissions validation, update external systems |
| `complete` | When task is completed | Validate submitted data, trigger side effects, update business entities |
| `delete` | When task is deleted | Cleanup resources, cancel pending operations |
| `all` | On any of the above events | Comprehensive logging, unified event handling |

### 6.2 Implementation Approaches

| Approach | Description | Best For |
|----------|-------------|----------|
| **Delegate Expression** | References a Spring bean by name using `${beanName}`. The bean implements `TaskListener` interface. | Production use - supports dependency injection and unit testing |
| **Class** | Specifies fully qualified class name that implements `TaskListener`. | Standalone listeners without Spring dependencies |
| **Expression** | Inline expression that calls a method on a Spring bean. | Simple, one-line operations like logging |

### 6.3 DelegateTask Properties

When a task listener is invoked, it receives a `DelegateTask` object that provides access to:

| Property Category | Available Information |
|-------------------|----------------------|
| **Task Info** | Task ID, name, definition key, description, priority, due date, create time |
| **Assignment** | Current assignee, owner, candidate users, candidate groups |
| **Process Context** | Process instance ID, process definition ID, execution ID |
| **Form** | Form key (for frontend routing) |
| **Event** | Event name that triggered the listener (create, assignment, complete, delete) |
| **Variables** | Read and write access to both process and task-local variables |

### 6.4 Execution Listeners

Execution listeners are similar to task listeners but fire on process and activity lifecycle events rather than task events.

| Event | Scope | Description |
|-------|-------|-------------|
| `start` | Process/Activity | Fires when a process instance starts or when execution enters an activity |
| `end` | Process/Activity | Fires when a process instance completes or when execution leaves an activity |
| `take` | Sequence Flow | Fires when a sequence flow (transition) is taken |

**Use Cases:**
- Log process start/end for auditing
- Initialize process-level variables at start
- Cleanup or finalize data when process ends
- Track activity transitions for monitoring

### 6.5 Best Practices

| Practice | Rationale |
|----------|-----------|
| Use delegate expressions | Enables Spring dependency injection and easier testing |
| Keep listeners focused | One listener, one responsibility - avoid complex multi-purpose listeners |
| Handle exceptions gracefully | Unhandled exceptions will fail the task operation |
| Avoid long-running operations | Move heavy processing to async jobs or service tasks |
| Use appropriate logging levels | INFO for significant events, DEBUG for detailed tracing |

---

## 7. Form Keys for Frontend Routing

### 7.1 How Form Keys Work

Form keys provide a bridge between the Flowable process definition and the frontend application, enabling dynamic routing based on task context.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           FORM KEY FLOW                                      │
│                                                                              │
│   BPMN Definition                                                            │
│   ┌─────────────────────────────────────────────────────────┐               │
│   │ <userTask id="makerTask"                                │               │
│   │           flowable:formKey="/maker/submit" ... />       │               │
│   └─────────────────────────────────────────────────────────┘               │
│                              │                                               │
│                              ▼                                               │
│   Backend API Response                                                       │
│   ┌─────────────────────────────────────────────────────────┐               │
│   │ {                                                       │               │
│   │   "taskId": "12345",                                    │               │
│   │   "taskName": "Submit Request",                         │               │
│   │   "formKey": "/maker/submit",     ◀── From BPMN         │               │
│   │   "processInstanceId": "67890"                          │               │
│   │ }                                                       │               │
│   └─────────────────────────────────────────────────────────┘               │
│                              │                                               │
│                              ▼                                               │
│   Frontend                                                                   │
│   ┌─────────────────────────────────────────────────────────┐               │
│   │ // Use formKey to navigate to appropriate component     │               │
│   │ navigate(task.formKey, {                                │               │
│   │   state: { taskId, processInstanceId, formKey }         │               │
│   │ });                                                     │               │
│   └─────────────────────────────────────────────────────────┘               │
│                              │                                               │
│                              ▼                                               │
│   Router Configuration                                                       │
│   ┌─────────────────────────────────────────────────────────┐               │
│   │ <Route path="/maker/submit" element={<SubmitForm />} /> │               │
│   └─────────────────────────────────────────────────────────┘               │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 7.2 Form Key Patterns

| Pattern | Description | Example |
|---------|-------------|---------|
| Role-based | Prefix with role | `/maker/submit`, `/checker/review` |
| Entity-based | Include entity type | `/order/create`, `/order/approve` |
| Action-based | Focus on action | `/submit`, `/review`, `/approve` |
| Hybrid | Combine approaches | `/maker/order/submit` |

### 7.3 Frontend Router Configuration

In React applications, configure routes that match form key patterns:

| Form Key | Route Configuration | Component |
|----------|--------------------|-----------|
| `/maker/submit` | `<Route path="/maker/submit" element={<SubmitForm />} />` | Maker submission form |
| `/checker/review` | `<Route path="/checker/review" element={<ReviewForm />} />` | Checker review form |
| `/admin/process` | `<Route path="/admin/process" element={<AdminProcess />} />` | Admin process view |

Wrap routes with `ProtectedRoute` component to enforce role-based access matching the BPMN `candidateGroups`.

### 7.4 Task Navigation Pattern

**Task List Flow:**
1. Fetch tasks for user's candidate groups
2. Display task list with name, create time, and action button
3. On "Claim & Open": call claim API, then navigate using `task.formKey`
4. Pass `taskId`, `processInstanceId`, and `formKey` via route state

### 7.5 Form Page Pattern

### 7.5 Form Page Pattern

**Form Page Flow:**
1. Retrieve `taskId`, `processInstanceId`, `formKey` from route state
2. Validate presence - redirect if missing (unauthorized access)
3. Render form fields for user input
4. On submit: save business data, then call complete task API with outcome variables
5. Navigate back to task list

### 7.6 Workflow Constants

**Recommended Constants to Define:**

| Constant Type | Examples | Purpose |
|--------------|----------|--------|
| Decision Variables | `stage1Decision`, `stage2Decision`, `finalDecision` | Variable names used in BPMN gateways |
| Decision Values | `APPROVE`, `REJECT`, `BACK`, `FORWARD` | Values passed when completing tasks |
| User Roles | `MAKER`, `CHECKER`, `ADMIN` | Must match BPMN `candidateGroups` |
| Form Key Patterns | `/maker/`, `/checker/`, `/admin/` | For route validation |

---

## 8. Process Variables

### 8.1 Variable Types Supported

| Type | Java Type | Description |
|------|-----------|-------------|
| `string` | `String` | Text values |
| `integer` | `Integer` | Whole numbers |
| `long` | `Long` | Large whole numbers |
| `double` | `Double` | Decimal numbers |
| `boolean` | `Boolean` | True/false |
| `date` | `Date` | Date/time values |
| `json` | `JsonNode` | JSON objects |
| `serializable` | `Serializable` | Complex Java objects |

### 8.2 Common Process Variables for Maker-Checker

| Variable | Type | Scope | Purpose |
|----------|------|-------|---------|
| `initiator` | String | Process | User who started the process |
| `decision` | String | Process | Approval decision (APPROVE/REJECT) |
| `action` | String | Task | Action performed (submit/save/cancel) |
| `rejectionReason` | String | Task | Reason for rejection |
| `businessKey` | String | Process | Correlation key to business entity |

### 8.3 Setting and Getting Variables

| Operation | Service | Method |
|-----------|---------|--------|
| Set process variable | RuntimeService | `setVariable(processInstanceId, name, value)` |
| Set multiple variables | RuntimeService | `setVariables(processInstanceId, variablesMap)` |
| Set task variable | TaskService | `setVariable(taskId, name, value)` |
| Set local task variable | TaskService | `setVariableLocal(taskId, name, value)` |
| Get variable | RuntimeService | `getVariable(processInstanceId, name)` |
| Get all variables | RuntimeService | `getVariables(processInstanceId)` |

**Note:** Local task variables are scoped to the task and not propagated to the process.

### 8.4 Variables in Task Completion

When completing a task, pass a variables map to `taskService.complete(taskId, variables)`:

| Common Variable | Purpose |
|-----------------|---------|
| `decision` | APPROVE or REJECT - drives gateway routing |
| `rejectionReason` | Explanation when rejecting |
| `comments` | User notes/feedback |

These variables become process variables and can be used in subsequent gateway conditions.

### 8.5 Variables in BPMN Conditions

Sequence flows from exclusive gateways use condition expressions to evaluate process variables:

| Condition | Expression | Result |
|-----------|------------|--------|
| Approve flow | `${decision == 'APPROVE'}` | Route to next task |
| Reject flow | `${decision == 'REJECT'}` | Route back to maker |
| Complex condition | `${decision == 'APPROVE' && amount > 1000}` | Route based on multiple criteria |

### 8.6 Transient Variables

Transient variables are **not persisted** to the database and exist only during the current execution:

| Use Case | Why Use Transient |
|----------|-------------------|
| Large payloads | Avoid storing large objects in ACT_RU_VARIABLE |
| Temporary calculations | Data only needed during current transaction |
| Sensitive data | Information that shouldn't be persisted |

**Methods:** `runtimeService.setTransientVariable()`, `taskService.complete(taskId, variables, transientVariables)`

---

## 9. Configuration

### 9.1 Application Properties

**Key Flowable Configuration Properties:**

| Property | Description | Recommended Value |
|----------|-------------|-------------------|
| `flowable.database-schema-update` | Schema update strategy | `true` for development |
| `flowable.process-definition-cache-limit` | Cache size for definitions | `128` |
| `flowable.history-level` | What history to record | `audit` for production |
| `flowable.async-executor-activate` | Enable async job execution | `true` for production |
| `flowable.async-executor.core-pool-size` | Async executor threads | `8` |
| `flowable.async-executor.max-pool-size` | Max async threads | `16` |
| `flowable.check-process-definitions` | Auto-deploy BPMN files | `true` |
| `flowable.process-definition-location-prefix` | BPMN file location | `classpath*:/processes/` |
| `flowable.process-definition-location-suffixes` | BPMN file patterns | `**.bpmn20.xml,**.bpmn` |

**Logging Levels:**
- `logging.level.org.flowable=INFO` - General Flowable logging
- `logging.level.org.flowable.engine.impl.persistence=DEBUG` - Entity operations

### 9.2 Spring Boot Configuration Class

For custom configuration beyond properties, create a `@Configuration` class with `SpringProcessEngineConfiguration` bean:

**Key Configuration Options:**
- `setDataSource()` - Database connection
- `setTransactionManager()` - Transaction handling
- `setDatabaseSchemaUpdate()` - Schema update strategy
- `setAsyncExecutorActivate()` - Enable async executor
- `setHistoryLevel()` - History recording level
- `setIdGenerator()` - Custom ID generation (e.g., `StrongUuidGenerator`)

### 9.3 History Levels

| Level | Recorded Data | Use Case |
|-------|---------------|----------|
| `none` | No history recorded | Development/testing only |
| `activity` | Process instances, activity instances | Basic tracking |
| `audit` | + Task instances, variable changes | **Recommended for production** |
| `full` | + All variable updates, form properties | Complete audit requirements |

### 9.4 Database Schema Update Options

| Value | Behavior |
|-------|----------|
| `false` | No schema changes; fails if schema doesn't match |
| `true` | Updates schema if needed; creates if missing |
| `create` | Creates schema; fails if exists |
| `create-drop` | Creates on startup, drops on shutdown |
| `drop-create` | Drops existing, then creates new |

---

## 10. Best Practices

### 10.1 Process Design

| Practice | Description |
|----------|-------------|
| **Use meaningful IDs** | `approvalTask` instead of `task1` |
| **Version your processes** | Flowable auto-versions, but document changes |
| **Keep processes simple** | Split complex logic into sub-processes |
| **Use gateways correctly** | Exclusive for XOR, Parallel for AND |
| **Add default flows** | Always have a default path from exclusive gateways |
| **Document conditions** | Add comments explaining gateway logic |

### 10.2 Task Listeners

| Practice | Description |
|----------|-------------|
| **Use delegate expressions** | Enables Spring dependency injection |
| **Keep listeners focused** | One responsibility per listener |
| **Handle exceptions** | Use try-catch and proper error handling |
| **Avoid long operations** | Move heavy work to async jobs |
| **Log appropriately** | Log at INFO for important events, DEBUG for details |

### 10.3 Variables

| Practice | Description |
|----------|-------------|
| **Use typed variables** | Avoid generic Object type |
| **Keep variables small** | Don't store large objects in process variables |
| **Clean up variables** | Remove temporary variables when no longer needed |
| **Use local variables** | For task-specific data that doesn't need process scope |
| **Use transient for large data** | Non-persisted variables for temporary large payloads |

### 10.4 Performance

| Practice | Description |
|----------|-------------|
| **Enable async executor** | For production environments |
| **Set appropriate cache sizes** | Based on number of active definitions |
| **Use appropriate history level** | `audit` for most cases, `full` only if needed |
| **Index frequently queried columns** | In custom application tables |
| **Clean up old history** | Schedule cleanup jobs for old process data |
| **Use query pagination** | Limit results in task/process queries |

### 10.5 Security

| Practice | Description |
|----------|-------------|
| **Validate inputs** | Always validate task variables before processing |
| **Check authorization** | Verify user has permission for claimed task |
| **Audit actions** | Log who did what and when |
| **Secure endpoints** | Use Spring Security for all Flowable APIs |
| **Don't expose internal IDs** | Use business keys for external references |

---

## 11. Troubleshooting

### 11.1 Common Issues

#### Task Not Found

```
org.flowable.common.engine.api.FlowableObjectNotFoundException: 
task 12345 doesn't exist
```

**Causes:**
- Task already completed
- Task ID is incorrect
- Task deleted

**Solution:** Verify task exists using `TaskService.createTaskQuery()`.

#### Process Definition Not Found

```
org.flowable.common.engine.api.FlowableObjectNotFoundException: 
no deployed process definition found with key 'processKey'
```

**Causes:**
- BPMN file not deployed
- Wrong process key
- Deployment failed

**Solution:** Check deployment exists and key matches BPMN file.

#### Gateway Condition Error

```
org.flowable.common.engine.api.FlowableException: 
No outgoing sequence flow of exclusive gateway 'gateway1' 
was selected
```

**Causes:**
- Required variable not set
- No condition matched

**Solution:** Ensure decision variable is set and add a default flow.

#### Duplicate Task Claim

```
org.flowable.common.engine.api.FlowableTaskAlreadyClaimedException: 
Task 'taskId' is already claimed by 'user1'
```

**Causes:**
- Concurrent claim attempts

**Solution:** Handle exception gracefully, refresh task list.

#### Optimistic Locking Exception

```
org.flowable.common.engine.api.FlowableOptimisticLockingException:
ProcessInstance was updated by another transaction concurrently
```

**Causes:**
- Multiple threads updating same process instance

**Solution:** Implement retry logic or synchronize access.

### 11.2 Debugging Queries

**Key Diagnostic Queries:**

| Purpose | Table | Key Columns |
|---------|-------|-------------|
| Check process state | `ACT_RU_EXECUTION` | `PROC_INST_ID_`, `ACT_ID_`, `IS_ACTIVE_` |
| Check current tasks | `ACT_RU_TASK` | `ASSIGNEE_`, `FORM_KEY_`, `CREATE_TIME_` |
| Check variables | `ACT_RU_VARIABLE` | `NAME_`, `VAR_TYPE_`, `TEXT_`, `LONG_` |
| Check failed jobs | `ACT_RU_DEADLETTER_JOB` | `EXCEPTION_MSG_`, `CREATE_TIME_` |
| Check deployments | `ACT_RE_DEPLOYMENT` + `ACT_RE_PROCDEF` | `KEY_`, `VERSION_`, `DEPLOY_TIME_` |

### 11.3 Logging Configuration

**Debugging Logging Levels:**

| Logger | Level | Purpose |
|--------|-------|--------|
| `org.flowable` | DEBUG | General Flowable operations |
| `org.flowable.engine.impl.persistence.entity` | DEBUG | Entity persistence |
| `org.flowable.task.service.impl.persistence` | DEBUG | Task operations |
| `org.flowable.job.service.impl.persistence` | DEBUG | Job processing |
| `org.flowable.engine.impl.db` | TRACE | SQL queries (very verbose) |

---

## Appendix A: Quick Reference

### A.1 Common TaskService Methods

**Query Methods:**
| Method | Purpose |
|--------|---------|
| `.taskAssignee(userId)` | Tasks assigned to user |
| `.taskCandidateGroup(groupId)` | Tasks claimable by group |
| `.taskCandidateUser(userId)` | Tasks where user is candidate |
| `.processInstanceId(procId)` | Tasks in specific process |
| `.taskDefinitionKey(key)` | Tasks by BPMN task ID |
| `.active()` | Only active tasks |
| `.orderByTaskCreateTime().desc()` | Sort by creation |
| `.listPage(offset, limit)` | Pagination |

**Action Methods:**
| Method | Purpose |
|--------|---------|
| `claim(taskId, userId)` | Assign task to user |
| `unclaim(taskId)` | Release task |
| `complete(taskId, variables)` | Complete with variables |
| `delegateTask(taskId, userId)` | Delegate to another user |
| `setAssignee(taskId, userId)` | Direct assignment |
| `addComment(taskId, procId, message)` | Add comment |

### A.2 Common RuntimeService Methods

**Start Process:**
| Method | Purpose |
|--------|---------|
| `startProcessInstanceByKey(key)` | Start by definition key |
| `startProcessInstanceByKey(key, variables)` | Start with initial variables |
| `startProcessInstanceByKey(key, businessKey, variables)` | Start with business correlation |

**Query & Control:**
| Method | Purpose |
|--------|---------|
| `.processDefinitionKey(key)` | Filter by process type |
| `.active()` | Only running instances |
| `.variableValueEquals(name, value)` | Filter by variable |
| `suspendProcessInstanceById(id)` | Pause execution |
| `activateProcessInstanceById(id)` | Resume execution |
| `deleteProcessInstance(id, reason)` | Cancel process |

**Variables:**
| Method | Purpose |
|--------|---------|
| `getVariable(procId, name)` | Get single variable |
| `getVariables(procId)` | Get all variables |
| `setVariable(procId, name, value)` | Set single variable |
| `setVariables(procId, map)` | Set multiple variables |

### A.3 Common HistoryService Methods

**Query Historic Processes:**
| Method | Purpose |
|--------|---------|
| `.finished()` | Completed only |
| `.unfinished()` | Running only |
| `.processDefinitionKey(key)` | By process type |
| `.startedBy(userId)` | By initiator |
| `.startedAfter(date)` | After date |
| `.orderByProcessInstanceEndTime().desc()` | Sort by completion |

**Query Historic Tasks:**
| Method | Purpose |
|--------|---------|
| `.processInstanceId(procId)` | Tasks in process |
| `.finished()` | Completed tasks |
| `.taskAssignee(userId)` | By assignee |

**Query Historic Variables:**
| Method | Purpose |
|--------|---------|
| `.processInstanceId(procId)` | Variables from process |
| `.variableName(name)` | Specific variable |

---

## Appendix B: Supported Databases

Flowable 7.2.0 supports the following databases:

| Database | Minimum Version | Notes |
|----------|-----------------|-------|
| H2 | 1.4.x | In-memory option for development |
| PostgreSQL | 10.x | Recommended for production |
| MySQL | 5.7.x / 8.0.x | InnoDB engine required |
| MariaDB | 10.x | InnoDB engine required |
| Oracle | 12c+ | Enterprise use |
| SQL Server | 2017+ | Enterprise use |
| DB2 | 11.1+ | Enterprise use |

---

## Appendix C: Resources

- [Flowable Documentation](https://www.flowable.com/open-source/docs/bpmn/ch02-GettingStarted)
- [BPMN 2.0 Specification](https://www.omg.org/spec/BPMN/2.0/)
- [Flowable GitHub Repository](https://github.com/flowable/flowable-engine)
- [Spring Boot Integration Guide](https://www.flowable.com/open-source/docs/bpmn/ch05a-Spring-Boot)
- [Flowable Forum](https://forum.flowable.org/)

---

*Document generated for Flowable 7.2.0 with Spring Boot integration*
