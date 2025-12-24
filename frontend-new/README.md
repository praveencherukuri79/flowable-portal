# Flowable Admin Dashboard

A modern admin dashboard for Flowable process management with maker-checker workflows.

## Features

- **Dashboard**: Overview with metrics, charts, and real-time activity
- **Running Instances**: View and manage active process instances
  - Filter by definition
  - Suspend, activate, or terminate instances
  - Assign tasks, add variables
- **Completed Instances**: Archive view of finished processes
  - Search and filter
  - View detailed history
- **Instance Details**: Comprehensive view with tabs
  - BPMN Diagram visualization
  - Process variables (view/edit)
  - Task history and management
  - Event timeline
- **Tasks**: Global task management
- **Events**: Real-time event log
- **Definitions**: View all deployed process definitions

## Tech Stack

- React 18 with TypeScript
- MUI (Material-UI) v5 with custom dark theme
- React Router v6 for navigation
- Recoil for state management
- Vite for fast development
- Axios for API calls

## Getting Started

### Prerequisites

- Node.js 18+ 
- npm or yarn
- Backend running on port 8080

### Installation

```bash
# Install dependencies
npm install

# Start development server
npm run dev
```

The app will be available at `http://localhost:3001`

### Build for Production

```bash
npm run build
npm run preview
```

## Project Structure

```
src/
├── api/           # API client services
│   ├── adminApi.ts    # Admin endpoints
│   └── authApi.ts     # Authentication
├── components/    # Reusable components
│   ├── Layout.tsx     # Main layout with sidebar
│   └── ProtectedRoute.tsx
├── pages/         # Page components
│   ├── Dashboard.tsx
│   ├── RunningInstances.tsx
│   ├── CompletedInstances.tsx
│   ├── InstanceDetails.tsx
│   ├── Tasks.tsx
│   ├── Events.tsx
│   ├── Definitions.tsx
│   └── Login.tsx
├── state/         # Recoil state atoms
├── theme/         # MUI theme configuration
├── types/         # TypeScript type definitions
├── App.tsx        # Root component with routing
└── main.tsx       # Entry point
```

## API Endpoints Used

The dashboard connects to the following backend endpoints:

### Admin APIs (`/api/admin`)
- `GET /definitions` - List process definitions
- `GET /instances/search` - Search instances
- `GET /tasks/search` - Search tasks
- `GET /events/search` - Get event logs
- `GET /metrics` - Dashboard metrics
- `GET /diagram/{id}` - Get BPMN diagram

### Flowable APIs (`/api/flowable`)
- `POST /runtime/suspend/{id}` - Suspend instance
- `POST /runtime/activate/{id}` - Activate instance
- `DELETE /runtime/{id}` - Delete instance
- `GET /runtime/{id}/variables` - Get variables
- `PUT /runtime/{id}/variables/{name}` - Set variable
- `POST /task/{id}/claim` - Claim task
- `POST /task/complete/{id}` - Complete task
- `GET /history/process/instance/{id}` - Get instance history
- `GET /history/task/process/{id}` - Get task history

### Enhanced Admin APIs (`/api/admin/v2`)
- `GET /instances/{id}/details` - Comprehensive instance details
- `POST /tasks/{id}/force-complete` - Force complete task
- `POST /instances/bulk-action` - Bulk operations
- `GET /statistics` - Enhanced statistics

## Configuration

The Vite dev server is configured to proxy API requests to `http://localhost:8080`. 
Update `vite.config.ts` if your backend runs on a different port.

## Design Reference

This dashboard is inspired by the designs in the `stitch_admin_dashboard` folder, 
featuring a dark theme optimized for admin workflows.

## License

MIT

