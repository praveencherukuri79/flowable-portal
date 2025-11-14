# Setup Guide

## Quick Start

### Backend Setup

1. Navigate to backend directory:
```bash
cd backend
```

2. Install dependencies and build:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

**Note:** The linter errors you see in the IDE are expected until Maven downloads the Flowable dependencies. After running `mvn clean install`, all dependencies will be resolved and the errors will disappear.

### Frontend Setup

1. Navigate to frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start development server:
```bash
npm run dev
```

The frontend will start on `http://localhost:3000`

**Note:** TypeScript errors about MUI modules are expected until `npm install` is run. After installing dependencies, all errors will be resolved.

## Troubleshooting

### Backend Errors

If you see import errors for Flowable classes:
- Run `mvn clean install` to download dependencies
- Make sure you have Java 17+ installed
- Check that Maven is properly configured

### Frontend Errors

If you see module resolution errors:
- Run `npm install` to install all dependencies
- Make sure Node.js 18+ is installed
- Clear node_modules and package-lock.json, then reinstall if needed

## Verification

After setup, you should be able to:
- Access backend at http://localhost:8080
- Access Swagger UI at http://localhost:8080/swagger-ui.html
- Access frontend at http://localhost:3000
- See the admin portal with all tabs working

