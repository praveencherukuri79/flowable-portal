# Developer Preferences & Guidelines

This document captures the developer's preferences and recommendations for working on this project.

---

## 📝 Documentation Policy

### ❌ DON'T:
- Create multiple .md files for every feature/fix
- Add step-by-step implementation guides
- Create verbose documentation files that clutter the root directory

### ✅ DO:
- Keep only essential documentation (README.md)
- Archive old documentation in `md_archive/`
- Explain in chat responses, not in files
- Use clear, concise commit messages

**Quote**: *"the read me files are growing out of hand"*

---

## 🏗️ Architecture Preferences

### Controllers
- **Use GENERIC controllers** for Flowable operations
- Controllers should be **clean and minimal** - delegate to services
- **DO NOT** create process-specific or BPMN-specific controllers
- Existing generic controllers:
  - `FlowableTaskController` - for task operations
  - `FlowableRuntimeController` - for runtime operations
  - `FlowableHistoryController` - for history
  - Use these instead of creating new ones

**Quote**: *"all flowable API's must be generic... we should not create flowable controller based on process or BPMN"*

**Quote**: *"why u need itemcontroller, plancontroller etc refactor properly"*

### Service Layer
- **Move ALL business logic to services**
- Controllers should be thin - just HTTP handling
- **Create reusable utils** to avoid duplication
- Services should handle:
  - Process management
  - Task management
  - Business entity operations

**Quote**: *"move all the business logic to services keep controllers clean create reusable utils"*

---

## 🔄 Flowable Workflow Principles

### SheetId Management
- **Every process** must have a `sheetId`
- SheetId should be **auto-generated** at process start
- SheetId should be stored in **process variables**
- Task listeners should **READ** sheetId from process variables
- Task listeners should **NEVER UPDATE** sheetId

**Quote**: *"add sheetid for every process"*

**Quote**: *"why would maker pass a sheet id, isnt that alredy there in process instance?"*

**Quote**: *"you are updating sheetid in delegates, which is incorret"*

### Task Listeners (Delegates)
- Task listeners execute business logic on task events
- They should READ variables from the task/process
- They should SAVE business entities (Products, Plans, Items)
- They should NOT modify process-level variables like sheetId

### Navigation & Access Control
- Users must access edit/approval pages **via formKey navigation**
- Users must **claim a task first**, then navigate from task list
- Edit/approval pages should **block direct URL access**
- FormKey in BPMN defines the page route (e.g., `/maker/product-edit`)

**Quote**: *"user has to click on formkey to naviagte to specific page right"*

---

## 🎯 Code Quality Standards

### Separation of Concerns
1. **Controllers**: HTTP layer only, minimal logic
2. **Services**: Business logic, orchestration
3. **Repositories**: Data access
4. **Task Listeners**: Flowable event handling
5. **Utils**: Reusable helper functions

### Reduce Duplication
- If similar code exists in multiple places, create a utility
- Reuse existing Flowable services (TaskService, RuntimeService, etc.)
- Don't duplicate query logic - centralize in utils

### Think Before Implementing
- Find the **root cause** before fixing
- Don't rush to implement
- Consider existing infrastructure before creating new components

**Quote**: *"Think again and again, find proper root cuase and then fix below dont rush"*

---

## 🚀 Git Commit Practices

### Commit Messages
- Be descriptive but concise
- Mention what was fixed and why
- Group related changes in single commit

### What to Commit
- Code changes
- Essential configuration
- README updates (when necessary)

### What NOT to Commit
- Verbose step-by-step documentation
- Temporary debug files
- Excessive .md files

---

## 📁 Project Organization

### File Structure
- Root directory: Only README.md and essential config
- Documentation archive: `md_archive/` (gitignored)
- Keep project structure clean and organized

---

## ⚡ Development Workflow

1. **Understand the requirement** thoroughly
2. **Think about root cause** - don't rush
3. **Check existing components** - reuse before creating
4. **Implement cleanly** - follow separation of concerns
5. **Explain in chat** - don't create documentation files
6. **Commit concisely** - clear message, grouped changes

---

## 🎨 UI/UX Principles

### Access Control
- Protect workflow pages from direct access
- Require proper task claim + formKey navigation
- Show clear error messages when access is denied
- Auto-redirect to appropriate portal

### Navigation Flow
1. User logs in
2. User goes to task list (My Tasks / Pending Approvals)
3. User claims a task
4. User clicks formKey button to navigate to edit/approval page
5. User completes task
6. System moves to next stage or completes process

---

## 📊 Entity Management

### Three-Stage Process
- **Stage 1**: Products
- **Stage 2**: Plans  
- **Stage 3**: Items

### Entity Separation
- Each stage has its own entity (Product, Plan, Item)
- Each entity has its own repository, service, DTO
- Each entity has its own task listener
- **Keep them independent** - don't mix in single files
- Each entity tracks its own `sheetId`

**Quote**: *"keep them independent keep sheetId separate"*

---

**Remember**: These are not rigid rules but strong preferences based on maintaining clean, maintainable, and scalable code.

