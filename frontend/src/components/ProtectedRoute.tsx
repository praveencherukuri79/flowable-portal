import React from 'react'
import { Navigate } from 'react-router-dom'
import { useRecoilValue } from 'recoil'
import { authState } from '../state/auth'

interface ProtectedRouteProps {
  children: React.ReactNode
  requiredRole?: string | string[]
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, requiredRole }) => {
  const auth = useRecoilValue(authState)

  if (!auth.isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  if (requiredRole) {
    const allowedRoles = Array.isArray(requiredRole) ? requiredRole : [requiredRole]
    
    // Admin has access to everything
    if (auth.role === 'ADMIN') {
      return <>{children}</>
    }

    // Check if user's role is in the allowed roles
    if (!allowedRoles.includes(auth.role || '')) {
      return <Navigate to="/unauthorized" replace />
    }
  }

  return <>{children}</>
}

