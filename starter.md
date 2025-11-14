# Flowable Workflow Application# Flowable Spring Boot Backend - Complete Implementation



A modern workflow management system built with Spring Boot, Flowable BPMN engine, and React + TypeScript.## 🚀 Project Overview

A comprehensive Spring Boot backend application featuring complete Flowable BPM integration with RESTful APIs, advanced caching, global error handling, and extensive OpenAPI documentation.

## 🚀 Project Overview

## ✅ Completed Features

This is a comprehensive maker-checker workflow application featuring:

- **Backend**: Spring Boot 3.x with Flowable BPMN engine### 1. **Complete Flowable Controllers (7/7)**

- **frontend**: Highly refactored React + TypeScript with Material-UIAll major Flowable controllers implemented with comprehensive REST APIs:

- **Features**: Process management, task assignments, BPMN visualization, analytics

- **FlowableTaskController**: Task management, assignments, completion

## 📊 Frontend Refactoring Highlights- **ProcessController**: Process instance lifecycle management  

- **FlowableHistoryController**: Historical data queries and analytics

The `frontend` directory contains a completely refactored version with **65% code reduction**:- **FlowableDeploymentController**: Deployment management and statistics

- **FlowableUserGroupController**: User and group management

| Component | Before | After | Reduction |- **FlowableModelController**: Process model CRUD operations

|-----------|--------|-------|-----------|- **FlowableEngineInfoController**: System health and diagnostics

| MakerDashboard | 390 lines | 87 lines | 78% |

| Tasks | 287 lines | 83 lines | 71% |### 2. **Comprehensive OpenAPI Documentation**

| Processes | 492 lines | 97 lines | 80% |All controllers feature extensive Swagger/OpenAPI annotations:

| Settings | 424 lines | 213 lines | 50% |- `@Operation` with detailed summaries and descriptions

| Analytics | 446 lines | 227 lines | 49% |- `@ApiResponse` for all possible HTTP responses

| **TOTAL** | **2,039 lines** | **707 lines** | **65%** |- `@Parameter` for all input parameters with examples

- `@Schema` annotations on all DTOs

### Key Improvements:- `@Tag` for organized API grouping

- ✅ Centralized theme system with Recoil state management

- ✅ Reusable MUI styled components (GlassCard, GradientButton, StyledDataGrid, etc.)### 3. **Lombok Integration & Code Cleanup**

- ✅ Custom hooks (useTheme, useUser, useTasks, useProcesses, useNotification, useConfirm)Complete transformation to eliminate boilerplate code:

- ✅ Utility functions (date formatting, color manipulation, validation, toast notifications)- All DTOs converted to use `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`

- ✅ Common components (StatsCard, TasksTable, ProcessStartDialog, ConfirmDialog, etc.)- RetentionOffer entity enhanced with Lombok and proper JPA annotations

- ✅ Zero compilation errors across all pages- Eliminated all manual getter/setter/equals/hashCode methods



## 🛠️ Tech Stack### 4. **Centralized DTO Mapping**

Created `DtoMapper` utility class with static methods:

### Backend- Consolidated all `toDTO` methods from services

- Java 17+- Builder pattern implementation for immutable DTOs

- Spring Boot 3.x- Comprehensive mapping for all Flowable entities

- Flowable BPMN Engine

- Spring Data JPA### 5. **Advanced Caching System**

- H2 Database (dev) / PostgreSQL (prod)Implemented strategic caching across all controllers:

- Maven- `@Cacheable` for read operations (GET endpoints)

- `@CacheEvict` for write operations (POST, PUT, DELETE)

### Frontend- Cache categories: tasks, processes, models, users, groups, deployments, history

- React 18

- TypeScript### 6. **Global Error Handling**

- Material-UI (MUI) v5Comprehensive error management with `GlobalExceptionHandler`:

- Recoil (state management)- FlowableException handling

- React Query (data fetching)- Validation error handling

- Recharts (analytics)- Generic exception handling

- Vite (build tool)- Consistent error response format

- dayjs (date manipulation)

### 7. **Service Architecture Enhancement**

## 📁 Project StructureOrganized service layer following Spring best practices:

- Interface-based service design

```- Implementation classes in `service.impl` package

copilot-test/- Dependency injection with `@Autowired`

├── backend/               # Spring Boot backend

│   ├── src/## 🔧 Technical Stack

│   │   ├── main/

│   │   │   ├── java/- **Framework**: Spring Boot 3.2.0

│   │   │   └── resources/- **BPM Engine**: Flowable 7.2.0

│   │   └── test/- **Documentation**: OpenAPI 3 (Swagger)

│   └── pom.xml- **Database**: H2 (development), JPA/Hibernate

├── frontend/              # Original React frontend- **Caching**: Spring Cache with ConcurrentMapCacheManager

├── frontend/             # Refactored frontend (recommended)- **Code Generation**: Lombok

│   ├── src/- **Build Tool**: Maven 3.9+

│   │   ├── components/- **Java Version**: 17+

│   │   │   ├── common/   # Reusable components

│   │   │   └── styled/   # MUI styled components## 🚀 Getting Started

│   │   ├── hooks/        # Custom React hooks

│   │   ├── pages/        # Page components### Prerequisites

│   │   ├── services/     # API services- Java 17 or higher

│   │   ├── state/        # Recoil atoms/selectors- Maven 3.9+

│   │   ├── store/        # Zustand stores

│   │   ├── theme/        # Theme configuration### Running the Application

│   │   ├── types/        # TypeScript types```bash

│   │   └── utils/        # Utility functionscd backend

│   └── package.jsonmvn spring-boot:run

└── README.md```

```

### Accessing APIs

## 🚀 Getting Started- **Application**: http://localhost:8080

- **Swagger UI**: http://localhost:8080/swagger-ui.html

### Prerequisites- **H2 Console**: http://localhost:8080/h2-console

- Java 17 or higher- **Health Check**: http://localhost:8080/api/engine/health

- Node.js 18+ and npm

- Maven 3.6+## 📚 API Documentation



### Backend SetupThe application provides comprehensive OpenAPI documentation with 50+ endpoints:

- **Tasks**: `/api/tasks/*` - Complete task lifecycle management

1. Navigate to backend directory:- **Processes**: `/api/processes/*` - Process instance operations

```bash- **History**: `/api/history/*` - Historical data and analytics

cd backend- **Deployments**: `/api/deployments/*` - Deployment management

```- **Users/Groups**: `/api/identity/*` - Identity management

- **Models**: `/api/models/*` - Process model management

2. Run the application:- **Engine**: `/api/engine/*` - System diagnostics and health

```bash

mvn spring-boot:run---

```

**Status**: ✅ **COMPLETED** - All requested features implemented and tested

The backend will start on `http://localhost:8080`**Build Status**: ✅ **SUCCESS** - Application compiles and runs successfully on port 8080



### Frontend Setup (frontend)---



1. Navigate to frontend directory:# Frontend: React + Vite + Material-UI + TypeScript

```bash

cd frontendModern frontend built with Vite for lightning-fast development, featuring Material-UI components, comprehensive API integration, and responsive design.

```

## ✅ Frontend Features

2. Install dependencies:- **🚀 Vite Build System**: Lightning-fast development builds and HMR

```bash- **🎨 Material-UI v6**: Modern component library with Grid2 layout system

npm install- **📱 Responsive Design**: Mobile-first responsive layout with drawer navigation

```- **🔌 API Integration**: Complete service layer for backend communication

- **🎯 TypeScript**: Full type safety and modern development experience

3. Start development server:- **🌙 Dark/Light Mode**: Built-in theme switching capability

```bash- **⚡ Performance**: Optimized with proper error handling and loading states

npm run dev

```---



The frontend will start on `http://localhost:5173`## How to build/run

- Backend: `cd backend && mvn spring-boot:run`

## 🔧 Configuration- Frontend: `cd frontend && npm run dev` (Vite dev server)



### Backend Configuration---

Edit `backend/src/main/resources/application.properties`:

- Database settings## To Do

- Flowable configuration- Implement user/admin portals

- Server port- Add maker-checker process

- CORS settings- Integrate BPMN.js and MUI X Charts

- Style for best UX

### Frontend Configuration
The frontend connects to `http://localhost:8080` by default. Update API base URL in:
- `frontend/src/services/api.service.ts`

## 📚 Available Scripts

### Backend
```bash
mvn clean install      # Build the project
mvn spring-boot:run    # Run the application
mvn test              # Run tests
```

### frontend
```bash
npm run dev           # Start development server
npm run build         # Build for production
npm run preview       # Preview production build
npm run lint          # Lint code
```

## 🎨 Features

### User Roles
- **Maker (User)**: Submit requests, view own tasks
- **Checker (Admin)**: Review/approve requests, manage all processes

### Core Features
1. **Dashboard**: Role-based dashboards with statistics
2. **Process Management**: Start, monitor, and manage BPMN processes
3. **Task Management**: Assign, complete, and track tasks
4. **Analytics**: Charts and metrics for process performance
5. **Settings**: Theme customization (light/dark mode, color themes)
6. **BPMN Visualization**: View process diagrams in real-time

### Theme System
- 🌓 Light/Dark mode toggle
- 🎨 6 color themes (Blue, Purple, Green, Orange, Pink, Teal)
- 💾 Preferences saved in localStorage
- ✨ Smooth transitions and animations

## 🧪 Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
```

## 📝 Documentation

Additional documentation available in `/md` directory:
- API documentation
- Refactoring summaries
- Testing methodology
- Component architecture

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 🙏 Acknowledgments

- Flowable BPMN Engine
- Material-UI team
- React community
- All contributors

## 📞 Contact

For questions or support, please open an issue in the repository.

---

**Built with ❤️ using Spring Boot, Flowable, and React**