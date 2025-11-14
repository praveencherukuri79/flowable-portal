# Authentication & Role-Based Access Guide

## ✅ Implemented Features

### Backend Authentication
- ✅ Spring Security with JWT
- ✅ User entity with roles (MAKER, CHECKER, ADMIN)
- ✅ Password encryption with BCrypt
- ✅ JWT token generation and validation
- ✅ Role-based endpoint protection

### User Roles

#### MAKER
- Create new requests/tasks
- View their own tasks
- Submit tasks for approval

#### CHECKER
- View pending approval tasks
- Approve or reject tasks
- Add checker comments

#### ADMIN
- Full system access
- User management (CRUD)
- View all processes, tasks, events
- Dashboard and metrics

---

## 🚀 Quick Start

### 1. Create Initial Users

After starting the backend, create users via API or directly in database:

```bash
# Create MAKER user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "maker1",
    "password": "password123",
    "email": "maker1@example.com",
    "fullName": "Maker One",
    "role": "MAKER"
  }'

# Create CHECKER user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "checker1",
    "password": "password123",
    "email": "checker1@example.com",
    "fullName": "Checker One",
    "role": "CHECKER"
  }'

# Create ADMIN user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "email": "admin@example.com",
    "fullName": "Administrator",
    "role": "ADMIN"
  }'
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "maker1",
    "password": "password123"
  }'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "maker1",
  "role": "MAKER",
  "fullName": "Maker One"
}
```

### 3. Use Token in Requests

```bash
curl -X GET http://localhost:8080/api/maker/my-tasks \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🔐 API Endpoints

### Public Endpoints (No Authentication)
- `POST /api/auth/login` - Login
- `POST /api/auth/register` - Register new user
- `/swagger-ui.html` - API documentation
- `/h2-console` - H2 database console (dev only)

### MAKER Endpoints
- `POST /api/maker/start-process` - Start new maker-checker process
- `GET /api/maker/my-tasks` - Get maker's tasks
- `POST /api/maker/complete-task/{taskId}` - Complete maker task

### CHECKER Endpoints
- `GET /api/checker/pending-tasks` - Get pending approval tasks
- `POST /api/checker/approve/{taskId}` - Approve task
- `POST /api/checker/reject/{taskId}` - Reject task

### ADMIN Endpoints
- All endpoints from MAKER and CHECKER
- `GET /api/admin/users` - Get all users
- `POST /api/admin/users` - Create user
- `PUT /api/admin/users/{id}` - Update user
- `DELETE /api/admin/users/{id}` - Delete user
- `GET /api/admin/definitions` - Process definitions
- `GET /api/admin/instances/search` - Process instances
- `GET /api/admin/tasks/search` - All tasks
- `GET /api/admin/events/search` - Event logs
- `GET /api/admin/metrics` - Dashboard metrics

---

## 🎯 Maker-Checker Workflow

### Step 1: Maker Creates Request
1. Maker logs in
2. Navigates to "Create Request"
3. Fills form:
   - Request Title
   - Description
   - Amount
   - Comments
4. Submits request
5. Process instance is created
6. Task moves to Checker queue

### Step 2: Checker Reviews Request
1. Checker logs in
2. Sees pending tasks in "Pending Approvals"
3. Reviews request details
4. Makes decision:
   - **APPROVE**: Process completes successfully
   - **REJECT**: Process ends with rejection
5. Adds comments
6. Submits decision

### Step 3: Process Completion
- If approved: Request is processed
- If rejected: Maker is notified
- Process history is recorded
- Metrics are updated

---

## 📊 BPMN Process Flow

```
[Start] 
   ↓
[Maker Task]
   ↓
[Checker Task]
   ↓
[Decision Gateway]
   ├─ APPROVE → [Approved End]
   └─ REJECT → [Rejected End]
```

**Process Variables:**
- `requestTitle` - Title of the request
- `requestDescription` - Detailed description
- `amount` - Financial amount (optional)
- `makerComments` - Maker's comments
- `checkerDecision` - APPROVE or REJECT
- `checkerComments` - Checker's review comments

---

## 🔧 Security Configuration

### JWT Settings

**File:** `backend/src/main/resources/application.properties`

```properties
# JWT Configuration
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=86400000  # 24 hours in milliseconds
```

### Role-Based Access Control

**Security Rules:**
- All `/api/admin/**` endpoints require `ADMIN` role
- All `/api/maker/**` endpoints require `MAKER` or `ADMIN` role
- All `/api/checker/**` endpoints require `CHECKER` or `ADMIN` role
- Authentication endpoints are public
- H2 console is accessible in development

---

## 🎨 Frontend Integration

### Authentication State Management

Store JWT token and user info in:
- Local Storage (persistent)
- Recoil state (runtime)

### Protected Routes

```typescript
// Route protection based on role
<Route path="/maker/*" element={
  <ProtectedRoute role="MAKER">
    <MakerPortal />
  </ProtectedRoute>
} />

<Route path="/checker/*" element={
  <ProtectedRoute role="CHECKER">
    <CheckerPortal />
  </ProtectedRoute>
} />

<Route path="/admin/*" element={
  <ProtectedRoute role="ADMIN">
    <AdminPortal />
  </ProtectedRoute>
} />
```

### API Client with Auth

```typescript
// Add auth header to all requests
axios.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})
```

---

## 🧪 Testing Authentication

### Test Flow

1. **Create Users**
   ```bash
   # Register 2 makers, 2 checkers, 1 admin
   ```

2. **Login as Maker**
   ```bash
   # Get token for maker1
   ```

3. **Create Request**
   ```bash
   # Start process as maker1
   ```

4. **Login as Checker**
   ```bash
   # Get token for checker1
   ```

5. **Approve/Reject**
   ```bash
   # Review and approve/reject as checker1
   ```

6. **Login as Admin**
   ```bash
   # View all processes and metrics
   ```

---

## 📝 Database Schema

### Users Table

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,  -- MAKER, CHECKER, ADMIN
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Flowable Tables
- `ACT_RU_TASK` - Active tasks
- `ACT_HI_TASKINST` - Task history
- `ACT_RU_EXECUTION` - Process instances
- `ACT_HI_PROCINST` - Process history
- And many more...

---

## 🚨 Security Best Practices

### Production Checklist

- [ ] Change default JWT secret
- [ ] Use environment variables for secrets
- [ ] Enable HTTPS
- [ ] Set appropriate JWT expiration
- [ ] Implement refresh tokens
- [ ] Add rate limiting
- [ ] Enable CORS only for trusted origins
- [ ] Remove H2 console in production
- [ ] Use strong password policies
- [ ] Implement account lockout
- [ ] Add audit logging
- [ ] Enable security headers

### Password Requirements

Current: Any password (for development)

Production should enforce:
- Minimum 8 characters
- Mix of uppercase/lowercase
- At least one number
- At least one special character
- Password history (prevent reuse)

---

## 🎉 Summary

✅ **Authentication**: JWT-based with Spring Security  
✅ **Authorization**: Role-based (MAKER, CHECKER, ADMIN)  
✅ **Process**: Maker-Checker BPMN workflow  
✅ **APIs**: Complete REST endpoints for all roles  
✅ **Security**: BCrypt passwords, JWT tokens, protected endpoints  

**Next Steps:**
1. Build frontend login page
2. Create maker/checker portals
3. Add role-based navigation
4. Implement logout functionality
5. Add user profile management

---

**Last Updated:** November 2024  
**Version:** 2.0.0

