# Flowable Three-Stage Maker-Checker Workflow

## Overview

This document describes the end-to-end flow of the three-stage maker-checker approval process for Items, Plans, and Products.

---

## Process Flow Diagram

```mermaid
graph TD
    Start([Process Start]) --> Stage3Maker[Stage 3: Maker Edit Items]
    Stage3Maker --> Stage3Checker[Stage 3: Checker Approve Items]
    Stage3Checker --> Stage3Gateway{Items Decision}
    Stage3Gateway -->|APPROVE| Stage2Maker[Stage 2: Maker Edit Plans]
    Stage3Gateway -->|REJECT| Stage3Maker
    
    Stage2Maker --> Stage2MakerGateway{Maker Decision}
    Stage2MakerGateway -->|Forward| Stage2Checker[Stage 2: Checker Approve Plans]
    Stage2MakerGateway -->|BACK| Stage3Maker
    
    Stage2Checker --> Stage2CheckerGateway{Plans Decision}
    Stage2CheckerGateway -->|APPROVE| Stage1Maker[Stage 1: Maker Edit Products]
    Stage2CheckerGateway -->|REJECT| Stage2Maker
    Stage2CheckerGateway -->|BACK| Stage3Maker
    
    Stage1Maker --> Stage1MakerGateway{Maker Decision}
    Stage1MakerGateway -->|Forward| Stage1Checker[Stage 1: Checker Approve Products]
    Stage1MakerGateway -->|BACK| Stage2Maker
    
    Stage1Checker --> Stage1CheckerGateway{Products Decision}
    Stage1CheckerGateway -->|APPROVE| Migration[Admin: Migrate to Production]
    Stage1CheckerGateway -->|REJECT| Stage1Maker
    Stage1CheckerGateway -->|BACK| Stage2Maker
    
    Migration --> End([Process Complete])
    
    style Start fill:#90EE90
    style End fill:#FFB6C1
    style Stage3Maker fill:#87CEEB
    style Stage2Maker fill:#87CEEB
    style Stage1Maker fill:#87CEEB
    style Stage3Checker fill:#FFD700
    style Stage2Checker fill:#FFD700
    style Stage1Checker fill:#FFD700
    style Migration fill:#DDA0DD
```

---

## Stage Details

### Stage 3: Items (First Stage)
- **Maker Task**: Edit Items (`/maker/item-edit`)
- **Checker Task**: Approve Items (`/checker/item-approval`)
- **Decision Options**: APPROVE → Stage 2, REJECT → Back to Stage 3 Maker

### Stage 2: Plans (Second Stage)
- **Maker Task**: Edit Plans (`/maker/plan-edit`)
- **Checker Task**: Approve Plans (`/checker/plan-approval`)
- **Decision Options**: 
  - APPROVE → Stage 1
  - REJECT → Back to Stage 2 Maker
  - BACK → Back to Stage 3 Maker

### Stage 1: Products (Final Stage)
- **Maker Task**: Edit Products (`/maker/product-edit`)
- **Checker Task**: Approve Products (`/checker/product-approval`)
- **Decision Options**:
  - APPROVE → Migration Task
  - REJECT → Back to Stage 1 Maker
  - BACK → Back to Stage 2 Maker

### Final: Data Migration
- **Admin Task**: Migrate Data (`/admin/data-migration`)
- **Action**: Migrates approved staging data to production tables

---

## Maker Submission Flow

```mermaid
sequenceDiagram
    participant User as Maker User
    participant UI as Maker Edit Page
    participant API as Backend API
    participant Task as Flowable Task
    participant Listener as TaskListener
    participant Sheet as Sheet Service
    participant Staging as Staging Service

    User->>UI: Navigate to Edit Page
    UI->>API: GET /api/data-query/maker-data/{processInstanceId}?entityType=item
    API->>Sheet: Find latest sheet by processInstanceId + sheetType
    alt Sheet Exists (Resubmit)
        Sheet-->>API: Return existing sheet
        API->>Staging: Get staging data by sheetId
        Staging-->>API: Return staging data
        API-->>UI: Return staging data with approval status
    else No Sheet (First Time)
        API->>Staging: Get MASTER data
        Staging-->>API: Return MASTER data
        API-->>UI: Return MASTER data
    end
    
    UI-->>User: Display data for editing
    
    User->>UI: Edit data and Submit
    UI->>Task: Complete task with reason="submit" + data
    Task->>Listener: Trigger TaskListener (on complete)
    
    Listener->>Listener: Validate: formKey contains "/maker/", reason="submit"
    Listener->>Sheet: Find sheet by processInstanceId + sheetType
    
    alt Sheet Exists
        Listener->>Staging: Load existing data
        Listener->>Sheet: Create new sheet (version++)
        Listener->>Listener: For each item: Compare with existing
        alt Data Unchanged
            Listener->>Listener: Preserve approval status
        else Data Changed/New
            Listener->>Listener: Clear approval status
        end
        Listener->>Staging: Save new data with new sheetId
    else No Sheet
        Listener->>Sheet: Create new sheet (version=1)
        Listener->>Staging: Save data with new sheetId
    end
    
    Listener->>Task: Set process variable: formKey-sheetId = newSheetId
    Task-->>User: Task completed, move to next stage
```

---

## Checker Approval Flow

```mermaid
sequenceDiagram
    participant User as Checker User
    participant UI as Checker Approval Page
    participant API as Backend API
    participant Sheet as Sheet Service
    participant Staging as Staging Service
    participant Task as Flowable Task

    User->>UI: Navigate to Approval Page
    UI->>API: GET /api/data-query/approval-data/{processInstanceId}?entityType=item
    API->>Sheet: Find latest sheet by processInstanceId + sheetType
    Sheet-->>API: Return latest sheet (highest version)
    API->>Staging: Get staging data by sheetId
    Staging-->>API: Return staging data
    API-->>UI: Return sheetId, sheet, and staging data
    UI-->>User: Display data for approval
    
    User->>UI: Approve individual rows or bulk approve
    UI->>API: POST /api/data-query/items/staging/approve-bulk/{sheetId}
    API->>Staging: Approve all rows for sheetId
    Staging-->>API: Success
    API-->>UI: Success
    
    User->>UI: Click "Approve Sheet & Complete Task"
    UI->>API: POST /api/data-query/sheets/{sheetId}/approve
    API->>Sheet: Approve sheet
    API->>Task: Complete task with itemDecision="APPROVE"
    Task-->>User: Task completed, move to next stage
```

---

## Version-Based SheetId Management

```mermaid
graph LR
    subgraph "First Submission"
        A1[Maker Submits] --> B1[Create Sheet v1]
        B1 --> C1[Save Data to Staging]
        C1 --> D1[SheetId: SHEET-ABC123 v1]
    end
    
    subgraph "Second Submission"
        A2[Maker Resubmits] --> B2[Find Latest Sheet v1]
        B2 --> C2[Create Sheet v2]
        C2 --> D2[Load Existing Data from v1]
        D2 --> E2[Compare & Process]
        E2 --> F2[Save New Data to Staging]
        F2 --> G2[SheetId: SHEET-XYZ789 v2]
        G2 --> H2[Old SheetId v1 Preserved]
    end
    
    subgraph "Checker Approval"
        I[Checker Views] --> J[Get Latest Sheet v2]
        J --> K[Approve Sheet v2]
        K --> L[Complete Task]
    end
    
    style D1 fill:#90EE90
    style G2 fill:#87CEEB
    style H2 fill:#FFD700
```

### Version Logic

1. **First Submission**: Creates sheet with `version = 1`
2. **Resubmission**: 
   - Finds latest sheet for `processInstanceId + sheetType`
   - Creates new sheet with `version = latestVersion + 1`
   - Old sheet data preserved for audit trail
3. **Checker Approval**: Always works with latest version (highest version number)

---

## Data Flow: Maker Submission

```mermaid
graph TD
    A[Maker Completes Task] --> B{Sheet Exists?}
    
    B -->|No| C[Create Sheet v1]
    C --> D[Set Metadata: PENDING]
    D --> E[Save to Staging]
    
    B -->|Yes| F[Load Existing Data]
    F --> G[Create Sheet v2]
    G --> H[For Each Item]
    H --> I{Data Changed?}
    I -->|No| J[Preserve Approval]
    I -->|Yes| K[Clear Approval]
    J --> L[Set Metadata]
    K --> L
    L --> M[Save to Staging]
    
    E --> N[Update Process Variable]
    M --> N
    N --> O[Task Complete]
```

---

## Data Flow: Checker Approval

```mermaid
graph TD
    A[Checker Views Task] --> B[Get Latest Sheet]
    B --> C[Load Staging Data]
    C --> D[Display Data]
    
    D --> E{Action}
    E -->|Approve Row| F[Approve Individual Row]
    E -->|Approve All| G[Bulk Approve All Rows]
    E -->|Approve Sheet| H[Approve Sheet]
    
    F --> I[Update Row Status]
    G --> I
    I --> J{All Rows Approved?}
    J -->|No| D
    J -->|Yes| K[Enable Approve Sheet Button]
    
    H --> L[Mark Sheet as APPROVED]
    L --> M[Complete Task with Decision]
    M --> N[Move to Next Stage]
```

---

## SheetId Version Tracking

### Database Structure

```
sheets table:
- id (PK)
- sheet_id (unique)
- process_instance_id
- sheet_type (item/plan/product)
- version (increments per submission)
- created_by
- created_at
- approved_by
- approved_at
- status (PENDING/APPROVED)
```

### Example Flow

```
Process Instance: PROC-123
Sheet Type: item

Submission 1:
  SheetId: SHEET-ABC123, Version: 1, Status: PENDING

Submission 2 (Resubmit):
  SheetId: SHEET-XYZ789, Version: 2, Status: PENDING
  (SHEET-ABC123 v1 preserved for audit)

Submission 3 (Resubmit):
  SheetId: SHEET-DEF456, Version: 3, Status: PENDING
  (SHEET-ABC123 v1, SHEET-XYZ789 v2 preserved)

Checker Approves:
  Uses SHEET-DEF456 (latest version = 3)
  Marks as APPROVED
```

---

## Key Components

### 1. Task Listeners
- **ItemTaskListener**: Handles item staging on maker submission
- **PlanTaskListener**: Handles plan staging on maker submission
- **ProductTaskListener**: Handles product staging on maker submission
- **DataMigrationTaskListener**: Handles final migration to production

### 2. Services
- **SheetService**: Manages sheet creation and versioning
- **StagingService**: Manages staging data (Item/Plan/Product)
- **DataMigrationService**: Migrates approved staging data to production

### 3. Process Variables
- `formKey-sheetId`: Stores sheetId for each stage (e.g., `/maker/item-edit-sheetId`)
- `reason`: Task completion reason ("submit" for maker tasks)
- `itemDecision`, `planDecision`, `productDecision`: Entity-based decision variables (replaces old stageXDecision)

---

## Navigation Rules

### Maker Navigation
1. User must **claim task** first from task list
2. Navigate via **formKey** button (not direct URL)
3. Can navigate **BACK** to previous stages (sets `stageXDecision = 'BACK'`)
4. **Submit** creates new sheet version

### Checker Navigation
1. User must **claim task** first from pending approvals
2. Navigate via **formKey** button
3. Can **APPROVE** (move forward), **REJECT** (send back), or **BACK** (previous stage)
4. Must approve **all rows** before approving sheet

---

## Approval Preservation Logic

When maker resubmits:

1. **Load existing data** from previous sheet version
2. **Compare** incoming data with existing data
3. **If unchanged**: Preserve approval status (approved, approvedBy, approvedAt)
4. **If changed**: Clear approval (set to PENDING)
5. **New items**: Start as PENDING

This ensures:
- Unchanged approved items remain approved
- Changed items require re-approval
- Full audit trail maintained

---

## Complete End-to-End Example

```
1. Admin starts process
   → Process Instance: PROC-123 created

2. Stage 3: Items
   → Maker claims task, edits items, submits
   → TaskListener creates Sheet v1 (SHEET-ITEM-001)
   → Data saved to item_staging table
   → Checker claims task, approves all rows, approves sheet
   → Process moves to Stage 2

3. Stage 2: Plans
   → Maker claims task, edits plans, submits
   → TaskListener creates Sheet v1 (SHEET-PLAN-001)
   → Data saved to plan_staging table
   → Checker approves, process moves to Stage 1

4. Stage 1: Products
   → Maker claims task, edits products, submits
   → TaskListener creates Sheet v1 (SHEET-PROD-001)
   → Data saved to product_staging table
   → Checker rejects, process goes back to Stage 1 Maker

5. Stage 1: Products (Resubmit)
   → Maker edits products again, submits
   → TaskListener finds existing Sheet v1
   → Creates Sheet v2 (SHEET-PROD-002)
   → Compares data, preserves approval for unchanged items
   → Checker approves, process moves to Migration

6. Migration
   → Admin migrates all approved staging data to production
   → Process completes
```

---

## Database Tables

### Staging Tables (Audit Trail)
- `item_staging`: All item submissions (preserved with sheetId)
- `plan_staging`: All plan submissions (preserved with sheetId)
- `product_staging`: All product submissions (preserved with sheetId)

### Production Tables
- `items`: Final approved items
- `plans`: Final approved plans
- `products`: Final approved products

### Sheet Management
- `sheets`: Tracks all sheet versions with version numbers

---

## Important Notes

1. **Version Increment**: Each maker submission creates a new sheet version
2. **Audit Trail**: All previous versions preserved in staging tables
3. **Latest Version**: Checker always works with latest version (highest version number)
4. **Approval Preservation**: Unchanged items keep their approval status
5. **Process Variables**: SheetId stored as `formKey-sheetId` (e.g., `/maker/item-edit-sheetId`)

