import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { Layout } from './components/Layout'
import { ProtectedRoute } from './components/ProtectedRoute'
import { Login } from './pages/Login'
import { Dashboard } from './pages/Dashboard'
import { RunningInstances } from './pages/RunningInstances'
import { CompletedInstances } from './pages/CompletedInstances'
import { InstanceDetails } from './pages/InstanceDetails'
import { Tasks } from './pages/Tasks'
import { Events } from './pages/Events'
import { Definitions } from './pages/Definitions'

// Placeholder pages for features not yet implemented
const Placeholder: React.FC<{ title: string }> = ({ title }) => (
  <div style={{ color: 'white', textAlign: 'center', padding: '40px' }}>
    <h2>{title}</h2>
    <p style={{ color: '#94a3b8' }}>This page is under construction</p>
  </div>
)

function App() {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/login" element={<Login />} />

      {/* Protected routes with Layout */}
      <Route
        element={
          <ProtectedRoute requiredRole={['ADMIN', 'MAKER', 'CHECKER']}>
            <Layout />
          </ProtectedRoute>
        }
      >
        {/* Dashboard */}
        <Route path="/dashboard" element={<Dashboard />} />

        {/* Instances */}
        <Route path="/instances/running" element={<RunningInstances />} />
        <Route path="/instances/completed" element={<CompletedInstances />} />
        <Route path="/instances/:instanceId" element={<InstanceDetails />} />

        {/* Tasks */}
        <Route path="/tasks" element={<Tasks />} />

        {/* Events */}
        <Route path="/events" element={<Events />} />

        {/* Definitions */}
        <Route path="/definitions" element={<Definitions />} />

        {/* Users & Settings (placeholder) */}
        <Route path="/users" element={<Placeholder title="Users & Groups" />} />
        <Route path="/settings" element={<Placeholder title="Settings" />} />

        {/* Default redirect */}
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
      </Route>

      {/* Catch all - redirect to dashboard */}
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}

export default App

