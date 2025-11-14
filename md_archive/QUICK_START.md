# 🚀 Quick Start Guide

## ✅ Everything is Ready!

Your complete Flowable Portal with Maker-Checker workflow is fully implemented and ready to run.

---

## 📦 What's Included

### ✅ Backend Features
- JWT Authentication
- 3 User Roles: MAKER, CHECKER, ADMIN
- Maker-Checker BPMN Workflow
- Complete REST APIs
- 5 Default Users (auto-created)
- Swagger Documentation
- H2 Database

### ✅ Frontend Features
- Login Page
- Maker Portal (Create requests, View tasks)
- Checker Portal (Approve/Reject)
- Admin Portal (6 tabs including User Management)
- MUI X Charts (6 chart types)
- Role-Based Access Control
- Modern, Responsive UI

---

## 🎯 Step 1: Start Backend (Port 8080)

Open a terminal:

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

**Wait for:** `Started BackendApplication`

### Default Users Created Automatically:
- **Maker 1**: `maker1` / `password123`
- **Maker 2**: `maker2` / `password123`
- **Checker 1**: `checker1` / `password123`
- **Checker 2**: `checker2` / `password123`
- **Admin**: `admin` / `admin123`

---

## 🎯 Step 2: Start Frontend (Port 3000)

Open a **NEW** terminal:

```bash
cd frontend
npm run dev
```

**Access at:** http://localhost:3000

---

## 🎮 Step 3: Test the Workflow

### Test 1: Login as Maker
1. Open http://localhost:3000
2. Login: `maker1` / `password123`
3. Click "Create Request" tab
4. Fill form:
   - Title: "New Customer Approval"
   - Description: "Approve new customer ABC Corp"
   - Amount: 100000
   - Comments: "Urgent approval needed"
5. Click "Create Request"
6. ✅ Success notification appears

### Test 2: Login as Checker
1. Logout (click user icon → Logout)
2. Login: `checker1` / `password123`
3. See pending task in "Pending Approvals"
4. Click "Approve" button
5. View request details
6. Add comments: "Approved"
7. Click "Approve"
8. ✅ Task disappears (process completed)

### Test 3: Login as Admin
1. Logout and Login: `admin` / `admin123`
2. **Dashboard**: See charts and metrics
3. **Definitions**: View process definitions
4. **Instances**: See completed processes
5. **Tasks**: View all tasks
6. **Events**: See event logs
7. **Users**: Manage users
   - Create new user
   - Edit existing user
   - Delete user

---

## 📊 Available URLs

### Frontend
- **App**: http://localhost:3000
- **Login**: http://localhost:3000/login
- **Maker Portal**: http://localhost:3000/maker
- **Checker Portal**: http://localhost:3000/checker
- **Admin Portal**: http://localhost:3000/admin

### Backend
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: (empty)
- **Health Check**: http://localhost:8080/actuator/health (if enabled)

---

## 🔑 Default Credentials

| Username | Password | Role | Access |
|----------|----------|------|--------|
| maker1 | password123 | MAKER | Create requests, view my tasks |
| maker2 | password123 | MAKER | Create requests, view my tasks |
| checker1 | password123 | CHECKER | Approve/reject requests |
| checker2 | password123 | CHECKER | Approve/reject requests |
| admin | admin123 | ADMIN | Full access to everything |

---

## 🎯 Complete Workflow Example

### 1️⃣ Maker Creates Request
```
Login: maker1 / password123
Navigate: Maker Portal → Create Request
Fill:
  - Title: "New Vendor Registration"
  - Description: "Register vendor XYZ Inc"
  - Amount: 50000
  - Comments: "Vendor needs to be onboarded ASAP"
Action: Create Request
Result: Process starts, task goes to checker queue
```

### 2️⃣ Checker Reviews & Approves
```
Login: checker1 / password123
Navigate: Checker Portal → Pending Approvals
View: Request details (all maker info visible)
Action: Approve with comments "Vendor verified and approved"
Result: Process completes successfully
```

### 3️⃣ Admin Views Everything
```
Login: admin / admin123
Navigate: Admin Portal → Instances
View: See the completed process
Navigate: Events
View: See complete audit trail
Navigate: Users
View: Manage all users in the system
```

---

## 🔍 API Testing with Swagger

1. Open http://localhost:8080/swagger-ui.html
2. Click "Authorize" button (🔒 icon)
3. Login to get token:
   - POST `/api/auth/login`
   - Body: `{ "username": "admin", "password": "admin123" }`
   - Copy the `token` from response
4. Click "Authorize" again
5. Enter: `Bearer <your-token>`
6. Now you can test all APIs!

---

## 🎨 Features to Explore

### Maker Portal
✅ Create unlimited requests  
✅ View personal task list  
✅ Track request status  

### Checker Portal
✅ See all pending approvals  
✅ View complete request details  
✅ Approve with optional comments  
✅ Reject with mandatory reason  

### Admin Portal
✅ **Dashboard**: 6 MUI X Charts  
✅ **Definitions**: BPMN process info  
✅ **Instances**: All process instances  
✅ **Tasks**: Complete task history  
✅ **Events**: Full audit log  
✅ **Users**: CRUD operations  

### Security Features
✅ JWT-based authentication  
✅ Role-based access control  
✅ Protected routes  
✅ Auto-logout on session expiry  
✅ Password encryption  

---

## 🛠️ Troubleshooting

### Backend won't start?
```bash
# Check Java version (requires Java 17+)
java -version

# Clean and rebuild
cd backend
mvn clean install -U

# Check port 8080 is available
netstat -ano | findstr :8080
```

### Frontend won't start?
```bash
# Reinstall dependencies
cd frontend
rm -rf node_modules
npm install

# Check port 3000 is available
netstat -ano | findstr :3000
```

### Can't login?
- Make sure backend is running
- Check console for errors
- Verify credentials (case-sensitive)
- Try: `admin` / `admin123`

### CORS errors?
- Restart backend
- Check `CorsConfig.java` allows `http://localhost:3000`
- Clear browser cache

---

## 📚 Documentation

- **Complete Guide**: `COMPLETE_GUIDE.md`
- **Authentication**: `AUTHENTICATION_GUIDE.md`
- **Implementation Status**: `IMPLEMENTATION_STATUS.md`
- **Requirements**: `REQUIREMENTS.md`
- **Setup Details**: `SETUP.md`

---

## 🎉 You're All Set!

1. ✅ Start backend: `cd backend && mvn spring-boot:run`
2. ✅ Start frontend: `cd frontend && npm run dev`
3. ✅ Open: http://localhost:3000
4. ✅ Login: `maker1` / `password123`
5. ✅ Test the workflow!

**Enjoy your fully functional Maker-Checker Workflow Application!** 🚀

---

**Questions?**
- Check Swagger: http://localhost:8080/swagger-ui.html
- Check H2 Console: http://localhost:8080/h2-console
- Review documentation files

**Happy Coding!** 💻✨

