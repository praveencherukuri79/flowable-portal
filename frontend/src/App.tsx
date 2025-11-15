import { Routes, Route, Navigate } from 'react-router-dom'
import { Layout } from './components/Layout'
import { ProtectedRoute } from './components/ProtectedRoute'
import { Login } from './pages/Login'
import { AdminPortal } from './pages/Admin/AdminPortal'
import { DataMigration } from './pages/Admin/DataMigration'
import { MakerPortal } from './pages/Maker/MakerPortal'
import { CheckerPortal } from './pages/Checker/CheckerPortal'
import { ProductEdit } from './pages/Maker/ProductEdit'
import { PlanEdit } from './pages/Maker/PlanEdit'
import { ItemEdit } from './pages/Maker/ItemEdit'
import { ProductApproval } from './pages/Checker/ProductApproval'
import { PlanApproval } from './pages/Checker/PlanApproval'
import { ItemApproval } from './pages/Checker/ItemApproval'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      
      <Route
        path="/admin"
        element={
          <ProtectedRoute requiredRole="ADMIN">
            <Layout>
              <AdminPortal />
            </Layout>
          </ProtectedRoute>
        }
      />
      
      <Route
        path="/admin/data-migration"
        element={
          <ProtectedRoute requiredRole="ADMIN">
            <Layout>
              <DataMigration />
            </Layout>
          </ProtectedRoute>
        }
      />
      
      <Route
        path="/maker"
        element={
          <ProtectedRoute requiredRole={['MAKER', 'ADMIN']}>
            <Layout>
              <MakerPortal />
            </Layout>
          </ProtectedRoute>
        }
      />
      
      <Route
        path="/maker/product-edit"
        element={
          <ProtectedRoute requiredRole={['MAKER', 'ADMIN']}>
            <Layout>
              <ProductEdit />
            </Layout>
          </ProtectedRoute>
        }
      />
      
      <Route
        path="/maker/plan-edit"
        element={
          <ProtectedRoute requiredRole={['MAKER', 'ADMIN']}>
            <Layout>
              <PlanEdit />
            </Layout>
          </ProtectedRoute>
        }
      />
      
      <Route
        path="/maker/item-edit"
        element={
          <ProtectedRoute requiredRole={['MAKER', 'ADMIN']}>
            <Layout>
              <ItemEdit />
            </Layout>
          </ProtectedRoute>
        }
      />
      
      <Route
        path="/checker"
        element={
          <ProtectedRoute requiredRole={['CHECKER', 'ADMIN']}>
            <Layout>
              <CheckerPortal />
            </Layout>
          </ProtectedRoute>
        }
      />
      
      <Route
        path="/checker/product-approval"
        element={
          <ProtectedRoute requiredRole={['CHECKER', 'ADMIN']}>
            <Layout>
              <ProductApproval />
            </Layout>
          </ProtectedRoute>
        }
      />
      
      <Route
        path="/checker/plan-approval"
        element={
          <ProtectedRoute requiredRole={['CHECKER', 'ADMIN']}>
            <Layout>
              <PlanApproval />
            </Layout>
          </ProtectedRoute>
        }
      />
      
      <Route
        path="/checker/item-approval"
        element={
          <ProtectedRoute requiredRole={['CHECKER', 'ADMIN']}>
            <Layout>
              <ItemApproval />
            </Layout>
          </ProtectedRoute>
        }
      />
      
      <Route path="/" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}

export default App
