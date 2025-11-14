
# Flowable Workflow Portal
### React 18 + TypeScript + MUI + MUI X Charts + Recoil + DayJS + Spring Boot 3.5.3 + Flowable 7.2.0 + OpenAPI + Lombok + JPA

This is the complete README.  
It includes ALL sections: backend, frontend, maker/checker, admin portal, metrics, BPMN, folder structure, requirements, etc.

---

# 1. Overview
This system is a complete workflow automation suite supporting:
- Maker → Checker approval flows
- BPMN workflow processing
- Admin portal with definitions, instances, tasks, events, metrics, diagrams
- User portal for business flows
- React + Spring Boot full-stack architecture


# 2. Architecture

## Backend
- Spring Boot 3.5.3
- Flowable 7.2.0
- JPA
- Lombok
- OpenAPI
- SQL Server/PostgreSQL
- SLF4J logging

## Frontend
- React 18
- TypeScript
- MUI
- MUI X DataGrid
- MUI X Charts
- Recoil
- DayJS

---

# 3. Maker → Checker Workflow

Maker submits → Checker reviews → Approve or Reject.

Variables:
- makerPayload
- makerComments
- checkerDecision
- checkerComments
- approvalTime

BPMN includes:
- Maker form task
- Checker review task
- Gateway controlling flow


# 4. Admin Portal (FULL FEATURE LIST)

Admin portal pages:

## Dashboard
- KPI cards
- Instances by day (bar chart)
- Tasks by state (pie chart)
- Avg durations (bar chart)
- Line trend graph
- Gauge indicator

## Definitions
List process definitions from Flowable.

## Instances
Search & paginate historic instances.

## Tasks
Search tasks by group/state.

## Events
Event logs from Flowable management service.

## Diagram Viewer
Fetch SVG diagram of workflow runtime state.

Admin API endpoints:

- GET /api/admin/definitions  
- GET /api/admin/instances/search  
- GET /api/admin/tasks/search  
- GET /api/admin/events/search  
- GET /api/admin/metrics  
- GET /api/admin/diagram/{id}


# 5. Backend Structure

```
com.example.flowableportal.admin
 ├─ AdminController
 ├─ AdminRuntimeService + Impl
 ├─ AdminTaskService + Impl
 ├─ AdminMetricsService + Impl
 ├─ DTOs (ProcessDefinitionDto, TaskDto, etc.)
 ├─ PagedResponse
 └─ DtoMapper
```

Flowable services used:
- RepositoryService
- RuntimeService
- TaskService
- HistoryService
- ManagementService

DTOs map all entities from the engine including:
- Tasks
- Instances
- Event logs
- Metrics


# 6. Frontend Structure

```
frontend/src/
 ├─ api/
 ├─ pages/Admin/
 ├─ components/
 ├─ recoil/
 ├─ theme/
 ├─ utils/dayjs.ts
 ├─ App.tsx
 └─ index.tsx
```

Admin portal pages:

### Dashboard.tsx
Uses MUI X Charts:
- BarChart
- LineChart
- PieChart
- Gauge

### Definitions.tsx
MUI DataGrid showing definitions.

### Instances.tsx
MUI DataGrid with pagination.

### Tasks.tsx
DataGrid with colored state chips.

### Events.tsx
DataGrid of Flowable events.

### Diagram Viewer
Fetch SVG from backend.


# 7. BPMN Requirements

All BPMN processes must include Maker and Checker tasks.

## Sequence:
1. Maker Task
2. Checker Task
3. Gateway: Approve / Reject
4. Approved path → downstream process
5. Rejected path → end event

Variables:
```
makerPayload
makerComments
checkerDecision
checkerComments
approvalTime
```

---

# 8. Deployment

## Backend
```
mvn clean install
java -jar target/*.jar
```

## Frontend
```
npm install
npm start
```

Backend runs at: http://localhost:8080  
Frontend at: http://localhost:3000

Swagger: /swagger-ui/index.html

---

# 9. Final Notes
This README contains the entire system description, architecture, backend API coverage, frontend pages, admin portal, BPMN flow rules, deployment, and folder structure.

