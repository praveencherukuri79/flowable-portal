# 🌊 Flowable Process Engine Portal

> A complete, production-ready Maker-Checker workflow application built with Spring Boot, Flowable, React, and Material-UI.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Flowable](https://img.shields.io/badge/Flowable-7.2.0-blue.svg)](https://flowable.com/)
[![React](https://img.shields.io/badge/React-18-61DAFB.svg)](https://reactjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.3-blue.svg)](https://www.typescriptlang.org/)
[![Material-UI](https://img.shields.io/badge/MUI-5-007FFF.svg)](https://mui.com/)

---

## ✨ Features

### 🔐 Authentication & Authorization
- **JWT-based authentication** with Spring Security
- **3 User Roles**: MAKER, CHECKER, ADMIN
- **Role-based access control** for all endpoints
- **5 Default users** created automatically on startup

### 📋 Maker-Checker Workflow
- **BPMN Process Engine** - Complete workflow automation
- **Maker Portal** - Create and submit approval requests
- **Checker Portal** - Review and approve/reject requests
- **Admin Portal** - Full system oversight and control

### 🎨 Modern UI
- **Material-UI (MUI) 5** - Beautiful, responsive design
- **MUI X DataGrid** - Advanced data tables with pagination
- **MUI X Charts** - 6 different chart types for analytics
- **TypeScript** - Type-safe development
- **Recoil** - State management

### 📊 Admin Dashboard
- Real-time metrics and analytics
- Process definitions management
- Instance monitoring
- Task tracking
- Event logging
- **User management** (Create, Edit, Delete)

---

## 🚀 Quick Start

### Prerequisites
- **Java 17+**
- **Maven 3.6+**
- **Node.js 18+**
- **npm or yarn**

### 1. Start Backend (Port 8080)

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

**Wait for:** `Started BackendApplication`

### 2. Start Frontend (Port 3000)

```bash
cd frontend
npm install
npm run dev
```

**Open:** http://localhost:3000

### 3. Login & Test

**Default Credentials:**
- **Maker**: `maker1` / `password123`
- **Checker**: `checker1` / `password123`
- **Admin**: `admin` / `admin123`

---

## 📖 Documentation

| Document | Description |
|----------|-------------|
| [QUICK_START.md](QUICK_START.md) | Step-by-step guide to run the application |
| [COMPLETE_GUIDE.md](COMPLETE_GUIDE.md) | Comprehensive documentation |
| [AUTHENTICATION_GUIDE.md](AUTHENTICATION_GUIDE.md) | Authentication & security details |
| [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md) | Feature completion status |

---

## 🏗️ Technology Stack

### Backend
- **Spring Boot 3.2.0** - Application framework
- **Flowable 7.2.0** - BPMN workflow engine
- **Spring Security** - Authentication & authorization
- **JWT** - Secure token-based auth
- **Spring Data JPA** - Database access
- **H2 Database** - In-memory database
- **Lombok** - Boilerplate reduction
- **OpenAPI/Swagger** - API documentation

### Frontend
- **React 18** - UI library
- **TypeScript** - Type safety
- **Material-UI 5** - Component library
- **MUI X DataGrid** - Advanced tables
- **MUI X Charts** - Data visualization
- **Recoil** - State management
- **Axios** - HTTP client
- **Day.js** - Date formatting
- **React Router v6** - Navigation
- **Vite** - Build tool

---

## 🎯 Key Workflows

### Maker Creates Request
1. Login as MAKER
2. Navigate to "Create Request"
3. Fill form (Title, Description, Amount, Comments)
4. Submit → Process starts

### Checker Reviews Request
1. Login as CHECKER
2. View "Pending Approvals"
3. Review request details
4. Approve/Reject with comments
5. Process completes

### Admin Manages System
1. Login as ADMIN
2. Dashboard - View metrics and charts
3. Users - Manage all users
4. Monitor processes, tasks, events
5. Full system control

---

## 🌐 API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration

### Maker APIs
- `POST /api/maker/start-process` - Start new request
- `GET /api/maker/my-tasks` - Get my tasks
- `POST /api/maker/complete-task/{taskId}` - Complete task

### Checker APIs
- `GET /api/checker/pending-tasks` - Get pending approvals
- `POST /api/checker/approve/{taskId}` - Approve request
- `POST /api/checker/reject/{taskId}` - Reject request

### Admin APIs
- `GET /api/admin/definitions` - Process definitions
- `GET /api/admin/instances/search` - Process instances
- `GET /api/admin/tasks/search` - All tasks
- `GET /api/admin/users` - User management

**Full API Docs:** http://localhost:8080/swagger-ui.html

---

## 🎨 Screenshots

### Login Page
Clean, modern login with role-based redirection.

### Maker Portal
- **Create Request Tab** - Form to submit new requests
- **My Tasks Tab** - View personal task list

### Checker Portal
- **Pending Approvals Tab** - Review and approve/reject requests
- Request details with complete variable display

### Admin Portal
- **Dashboard** - 6 MUI X Charts (Line, Bar, Pie, Gauge, Sparkline, Area)
- **Definitions** - BPMN process definitions
- **Instances** - Process instance monitoring
- **Tasks** - Complete task history
- **Events** - Full audit trail
- **Users** - User management with CRUD operations

---

## 📊 Charts & Analytics

The Admin Dashboard includes 6 different chart types:

1. **Line Chart** - Process instances over time
2. **Bar Chart** - Tasks by status
3. **Pie Chart** - Process definitions distribution
4. **Gauge Chart** - Task completion rate
5. **Sparkline Chart** - Recent activity trend
6. **Area/Heatmap Chart** - Event distribution

---

## 🔒 Security Features

✅ **JWT Authentication** - Stateless, secure tokens  
✅ **Password Encryption** - BCrypt hashing  
✅ **Role-Based Access** - Granular permissions  
✅ **Protected Routes** - Frontend route guards  
✅ **CORS Configuration** - Secure cross-origin requests  
✅ **Method Security** - `@PreAuthorize` annotations  
✅ **Auto-Logout** - On token expiration  

---

## 🛠️ Development

### Backend Development

```bash
cd backend

# Run tests
mvn test

# Package
mvn package

# Run with profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Frontend Development

```bash
cd frontend

# Development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

---

## 📦 Project Structure

```
flowable-portal/
├── backend/
│   ├── src/main/java/com/example/backend/
│   │   ├── config/              # Configuration classes
│   │   ├── controller/          # REST controllers
│   │   ├── dto/                 # Data Transfer Objects
│   │   ├── model/               # JPA entities
│   │   ├── repository/          # Spring Data repositories
│   │   ├── security/            # JWT & Security
│   │   ├── service/             # Business logic
│   │   └── util/                # Utilities
│   ├── src/main/resources/
│   │   ├── processes/           # BPMN files
│   │   └── application.properties
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── api/                 # API clients
│   │   ├── components/          # Reusable components
│   │   ├── pages/
│   │   │   ├── Admin/          # Admin portal
│   │   │   ├── Maker/          # Maker portal
│   │   │   ├── Checker/        # Checker portal
│   │   │   └── Login.tsx
│   │   ├── state/              # Recoil atoms
│   │   ├── theme/              # MUI theme
│   │   └── App.tsx
│   └── package.json
│
└── Documentation/
    ├── README.md                # This file
    ├── QUICK_START.md          # Quick start guide
    ├── COMPLETE_GUIDE.md       # Full documentation
    └── AUTHENTICATION_GUIDE.md # Auth details
```

---

## 🧪 Testing

### Test Users

| Username | Password | Role | Purpose |
|----------|----------|------|---------|
| maker1 | password123 | MAKER | Create requests |
| maker2 | password123 | MAKER | Create requests |
| checker1 | password123 | CHECKER | Approve/reject |
| checker2 | password123 | CHECKER | Approve/reject |
| admin | admin123 | ADMIN | Full access |

### Test Scenario

1. **Login as maker1** → Create request
2. **Login as checker1** → Approve request
3. **Login as admin** → View dashboard & metrics

---

## 🐛 Troubleshooting

### Backend Issues
- Ensure Java 17+ is installed
- Check port 8080 is available
- Verify Maven dependencies: `mvn clean install`

### Frontend Issues
- Clear node_modules: `rm -rf node_modules && npm install`
- Check port 3000 is available
- Verify Node.js 18+ is installed

### Login Issues
- Ensure backend is running on port 8080
- Check browser console for errors
- Try admin credentials: `admin` / `admin123`

---

## 🔗 Useful Links

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: (empty)
- **Frontend**: http://localhost:3000

---

## 📝 License

This project is licensed under the MIT License.

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

## 📧 Support

For questions and support, please refer to the documentation files or open an issue.

---

## 🎉 Acknowledgments

- **Flowable** - Powerful BPMN engine
- **Spring Boot** - Excellent Java framework
- **Material-UI** - Beautiful React components
- **React Team** - Amazing library

---

**Built with ❤️ using Spring Boot, Flowable, React, and Material-UI**

---

## 🚀 Get Started Now!

```bash
# Clone the repository
git clone <your-repo-url>

# Start backend
cd backend && mvn spring-boot:run

# Start frontend (in new terminal)
cd frontend && npm install && npm run dev

# Open browser
# http://localhost:3000
# Login: maker1 / password123
```

**Happy Coding!** 🎊
